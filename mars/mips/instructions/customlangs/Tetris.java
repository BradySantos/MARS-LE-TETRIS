package mars.mips.instructions.customlangs;
import mars.simulator.*;
import mars.mips.hardware.*;
import mars.mips.instructions.syscalls.*;
import mars.*;
import mars.util.*;
import java.util.*;
import java.io.*;
import mars.mips.instructions.*;
import java.util.Random;


public class Tetris extends CustomAssembly {
    @Override
    public String getName() {
        return "Tetris";
    }

    @Override
    public String getDescription() {
        return "This is language based on a simplified version of Tetris";
    }

    @Override
    protected void populate() {
    // Basic instructions

        // harddrop (addi)
        instructionList.add(
                new BasicInstruction("harddrop $t1,$t2,-100",
                        "Add immediate value to reg: $t0 = $t1 + imm",
                        BasicInstructionFormat.I_FORMAT,
                        "001000 sssss fffff tttttttttttttttt",
                        new SimulationCode()
                        {
                            public void simulate(ProgramStatement statement) throws ProcessingException
                            {
                                int[] operands = statement.getOperands();
                                int add1 = RegisterFile.getValue(operands[1]);
                                int add2 = operands[2] << 16 >> 16;
                                int sum = add1 + add2;
                                // overflow on A+B detected when A and B have same sign and A+B has other sign.
                                if ((add1 >= 0 && add2 >= 0 && sum < 0)
                                        || (add1 < 0 && add2 < 0 && sum >= 0))
                                {
                                    throw new ProcessingException(statement,
                                            "arithmetic overflow",Exceptions.ARITHMETIC_OVERFLOW_EXCEPTION);
                                }
                                RegisterFile.updateRegister(operands[0], sum);
                            }
                        }));

        // attack (mul)
        instructionList.add(
                new BasicInstruction("attack $t1,$t2,$t3",
                        "Set HI to high-order 32 bits, LO and $t1 to low-order 32 bits of the product of $t2 and $t3",
                        BasicInstructionFormat.R_FORMAT,
                        "011100 sssss ttttt fffff 00000 000010",
                        new SimulationCode()
                        {
                            public void simulate(ProgramStatement statement) throws ProcessingException
                            {
                                int[] operands = statement.getOperands();
                                long product = (long) RegisterFile.getValue(operands[1])
                                        * (long) RegisterFile.getValue(operands[2]);
                                RegisterFile.updateRegister(operands[0],
                                        (int) ((product << 32) >> 32));
                                // Register 33 is HIGH and 34 is LOW.  Not required by MIPS; SPIM does it.
                                RegisterFile.updateRegister(33, (int) (product >> 32));
                                RegisterFile.updateRegister(34, (int) ((product << 32) >> 32));
                            }
                        }));

        // delay (div)
        instructionList.add(
                new BasicInstruction("delay $t1,$t2",
                        "Divide $t1 by $t2 then set LO to quotient and HI to remainder",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 fffff sssss 00000 00000 011010",
                        new SimulationCode()
                        {
                            public void simulate(ProgramStatement statement) throws ProcessingException
                            {
                                int[] operands = statement.getOperands();
                                if (RegisterFile.getValue(operands[1]) == 0)
                                {
                                    // Note: no exceptions and undefined results for zero div
                                    // COD3 Appendix A says "with overflow" but MIPS 32 instruction set
                                    // specification says "no arithmetic exception under any circumstances".
                                    return;
                                }

                                // Register 33 is HIGH and 34 is LOW
                                RegisterFile.updateRegister(33,
                                        RegisterFile.getValue(operands[0])
                                                % RegisterFile.getValue(operands[1]));
                                RegisterFile.updateRegister(34,
                                        RegisterFile.getValue(operands[0])
                                                / RegisterFile.getValue(operands[1]));
                            }
                        }));

        // stack (add)
        instructionList.add(
                new BasicInstruction("stack $t1,$t2,$t3",
                        "Add two registers: $t0 = $t1 + $t2",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 sssss ttttt fffff 00000 100000",
                        new SimulationCode()
                        {
                            public void simulate(ProgramStatement statement) throws ProcessingException
                            {
                                int[] operands = statement.getOperands();
                                int add1 = RegisterFile.getValue(operands[1]);
                                int add2 = RegisterFile.getValue(operands[2]);
                                int sum = add1 + add2;
                                // overflow on A+B detected when A and B have same sign and A+B has other sign.
                                if ((add1 >= 0 && add2 >= 0 && sum < 0)
                                        || (add1 < 0 && add2 < 0 && sum >= 0))
                                {
                                    throw new ProcessingException(statement,
                                            "arithmetic overflow",Exceptions.ARITHMETIC_OVERFLOW_EXCEPTION);
                                }
                                RegisterFile.updateRegister(operands[0], sum);
                            }
                        }));

        // garbage (sub)
        instructionList.add(
                new BasicInstruction("garbage $t1,$t2,$t3",
                        "Subtract two registers: $t0 = $t1 - $t2",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 sssss ttttt fffff 00000 100010",
                        new SimulationCode()
                        {
                            public void simulate(ProgramStatement statement) throws ProcessingException
                            {
                                int[] operands = statement.getOperands();
                                int sub1 = RegisterFile.getValue(operands[1]);
                                int sub2 = RegisterFile.getValue(operands[2]);
                                int dif = sub1 - sub2;
                                // overflow on A-B detected when A and B have opposite signs and A-B has B's sign
                                if ((sub1 >= 0 && sub2 < 0 && dif < 0)
                                        || (sub1 < 0 && sub2 >= 0 && dif >= 0))
                                {
                                    throw new ProcessingException(statement,
                                            "arithmetic overflow",Exceptions.ARITHMETIC_OVERFLOW_EXCEPTION);
                                }
                                RegisterFile.updateRegister(operands[0], dif);
                            }
                        }));

        // hold (sw)
        instructionList.add(
                new BasicInstruction("hold $t1,-100($t2)",
                        "Store word into memory: $t0 --> imm($t1)",
                        BasicInstructionFormat.I_FORMAT,
                        "101011 ttttt fffff ssssssssssssssss",
                        new SimulationCode()
                        {
                            public void simulate(ProgramStatement statement) throws ProcessingException
                            {
                                int[] operands = statement.getOperands();
                                try
                                {
                                    Globals.memory.setWord(
                                            RegisterFile.getValue(operands[2]) + operands[1],
                                            RegisterFile.getValue(operands[0]));
                                }
                                catch (AddressErrorException e)
                                {
                                    throw new ProcessingException(statement, e);
                                }
                            }
                        }));

        // next (lw)
        instructionList.add(
                new BasicInstruction("next $t1,-100($t2)",
                        "Load word from memory: imm($t1) --> $t0",
                        BasicInstructionFormat.I_FORMAT,
                        "100011 ttttt fffff ssssssssssssssss",
                        new SimulationCode()
                        {
                            public void simulate(ProgramStatement statement) throws ProcessingException
                            {
                                int[] operands = statement.getOperands();
                                try
                                {
                                    RegisterFile.updateRegister(operands[0],
                                            Globals.memory.getWord(
                                                    RegisterFile.getValue(operands[2]) + operands[1]));
                                }
                                catch (AddressErrorException e)
                                {
                                    throw new ProcessingException(statement, e);
                                }
                            }
                        }));

        // shift (j)
        instructionList.add(
                new BasicInstruction("shift target",
                        "Jump unconditionally to target address",
                        BasicInstructionFormat.J_FORMAT,
                        "000010 ffffffffffffffffffffffffff",
                        new SimulationCode()
                        {
                            public void simulate(ProgramStatement statement) throws ProcessingException
                            {
                                int[] operands = statement.getOperands();
                                Globals.instructionSet.processJump(
                                        ((RegisterFile.getProgramCounter() & 0xF0000000)
                                                | (operands[0] << 2)));
                            }
                        }));

        // lineclear (beq)
        instructionList.add(
                new BasicInstruction("lineclear $t1,$t2,label",
                        "Jump to label if $t1 == $t2",
                        BasicInstructionFormat.I_BRANCH_FORMAT,
                        "000100 fffff sssss tttttttttttttttt",
                        new SimulationCode()
                        {
                            public void simulate(ProgramStatement statement) throws ProcessingException
                            {
                                int[] operands = statement.getOperands();

                                if (RegisterFile.getValue(operands[0])
                                        == RegisterFile.getValue(operands[1]))
                                {
                                    Globals.instructionSet.processBranch(operands[2]);
                                }
                            }
                        }));

        // nonclear (bne)
        instructionList.add(
                new BasicInstruction("nonclear $t1,$t2,label",
                        "Jump to label if $t1 != $t2",
                        BasicInstructionFormat.I_BRANCH_FORMAT,
                        "000101 fffff sssss tttttttttttttttt",
                        new SimulationCode()
                        {
                            public void simulate(ProgramStatement statement) throws ProcessingException
                            {
                                int[] operands = statement.getOperands();
                                if (RegisterFile.getValue(operands[0])
                                        != RegisterFile.getValue(operands[1]))
                                {
                                    Globals.instructionSet.processBranch(operands[2]);
                                }
                            }
                        }));

        // Unique instructions

        // downstack
        instructionList.add(
                new BasicInstruction("downstack",
                        "If registers $t0 to $t9 have at least 1, decrease all by 1 and increase score in $s0 by 1",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 00000000000000000000 111111",
                        new SimulationCode()
                        {
                            public void simulate(ProgramStatement statement) throws ProcessingException
                            {
                                boolean canScore = true;
                                for (int i = 8; i < 16; i++) { // $t0 to $t7
                                    int baseVal = RegisterFile.getValue(i);
                                    if (baseVal < 1) { // Register values must be >= 1
                                        canScore = false;
                                    }
                                }
                                for (int i = 24; i < 26; i++) { // $t8 to $t9
                                    int baseVal = RegisterFile.getValue(i);
                                    if (baseVal < 1) {
                                        canScore = false;
                                    }
                                }

                                if (canScore) {
                                    for (int i = 8; i < 16; i++) { // $t0 to $t7
                                        int baseVal = RegisterFile.getValue(i);
                                        RegisterFile.updateRegister(i, baseVal - 1);
                                    }
                                    for (int i = 24; i < 26; i++) { // $t8 to $t9
                                        int baseVal = RegisterFile.getValue(i);
                                        RegisterFile.updateRegister(i, baseVal - 1);
                                        }
                                    int scoreVal = RegisterFile.getValue(16); // Update score by 1
                                    RegisterFile.updateRegister(16, scoreVal + 1);
                                    }
                                }
                            }
                        ));

        // topout
        instructionList.add(
                new BasicInstruction("topout",
                        "If any registers $t0 to $t9 are 20 or greater, set all of them to 0, set score in $s0 to 0, print final score",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 00000000000000000000 111110",
                        new SimulationCode()
                        {
                            public void simulate(ProgramStatement statement) throws ProcessingException
                            {
                                boolean toppedOut = false;
                                for (int i = 8; i < 16; i++) { // $t0 to $t7
                                    int baseVal = RegisterFile.getValue(i);
                                    if (baseVal >= 20) { // Register values must be < 20
                                        toppedOut = true;
                                    }
                                }
                                for (int i = 24; i < 26; i++) { // $t8 to $t9
                                    int baseVal = RegisterFile.getValue(i);
                                    if (baseVal >= 20) {
                                        toppedOut = true;
                                    }
                                }

                                if (toppedOut) {
                                    for (int i = 8; i < 16; i++) { // $t0 to $t7
                                        RegisterFile.updateRegister(i, 0); // Set registers to 0
                                    }
                                    for (int i = 24; i < 26; i++) { // $t8 to $t9
                                        RegisterFile.updateRegister(i, 0);
                                    }
                                    int scoreVal = RegisterFile.getValue(16); // Store score
                                    RegisterFile.updateRegister(16, 0); // Set score to 0

                                    SystemIO.printString("Your final score was " + scoreVal + "!\nResetting game.\n");
                                }
                            }
                        }));

        // allclear
        instructionList.add(
                new BasicInstruction("allclear",
                        "If all registers $t0 to $t9 are 0, double score in $s0",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 00000000000000000000 111101",
                        new SimulationCode()
                        {
                            public void simulate(ProgramStatement statement) throws ProcessingException
                            {
                                boolean cleared = true;
                                for (int i = 8; i < 16; i++) { // $t0 to $t7
                                    int baseVal = RegisterFile.getValue(i);
                                    if (baseVal != 0) { // Register values must be 0
                                        cleared = false;
                                    }
                                }
                                for (int i = 24; i < 26; i++) { // $t8 to $t9
                                    int baseVal = RegisterFile.getValue(i);
                                    if (baseVal != 0) {
                                        cleared = false;
                                    }
                                }

                                if (cleared) {
                                    int scoreVal = RegisterFile.getValue(16); // Double score
                                    RegisterFile.updateRegister(16, scoreVal * 2);
                                    SystemIO.printString("All clear! Doubling score.\n");
                                }
                            }
                        }));

        // viewboard
        instructionList.add(
                new BasicInstruction("viewboard",
                        "Print out all values of registers $t0 to $t9",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 00000000000000000000 111011",
                        new SimulationCode()
                        {
                            public void simulate(ProgramStatement statement) throws ProcessingException
                            {
                                SystemIO.printString("The current board is: \n");
                                for (int i = 0; i < 10; i++) { // Print register names
                                    SystemIO.printString("$t" + i + " ");
                                }
                                SystemIO.printString("\n");

                                for (int i = 8; i < 16; i++) { // $t0 to $t7
                                    int regVal = RegisterFile.getValue(i);
                                    SystemIO.printString(regVal + " ");
                                }
                                for (int i = 24; i < 26; i++) { // $t8 to $t9
                                    int regVal = RegisterFile.getValue(i);
                                    SystemIO.printString(regVal + " ");
                                }

                                SystemIO.printString("\n");
                            }
                        }));

        // tetris
        instructionList.add(
                new BasicInstruction("tetris",
                        "If all values in registers $t0 to $t9 are at least 4, subtract 4 from each and increase score in $s0 by 5. Print Tetris!",
                        BasicInstructionFormat.R_FORMAT,
                        "000000 00000000000000000000 110111",
                        new SimulationCode()
                        {
                            public void simulate(ProgramStatement statement) throws ProcessingException
                            {
                                boolean quad = true;
                                for (int i = 8; i < 16; i++) { // $t0 to $t7
                                    int baseVal = RegisterFile.getValue(i);
                                    if (baseVal < 4) { // Register values must be >= 4
                                        quad = false;
                                    }
                                }
                                for (int i = 24; i < 26; i++) { // $t8 to $t9
                                    int baseVal = RegisterFile.getValue(i);
                                    if (baseVal < 4) {
                                        quad = false;
                                    }
                                }

                                if (quad) {
                                    for (int i = 8; i < 16; i++) { // $t0 to $t7
                                        int baseVal = RegisterFile.getValue(i);
                                        RegisterFile.updateRegister(i, baseVal - 4); // Subtract 4 from each register
                                    }
                                    for (int i = 24; i < 26; i++) { // $t8 to $t9
                                        int baseVal = RegisterFile.getValue(i);
                                        RegisterFile.updateRegister(i, baseVal - 4);
                                    }
                                    int scoreVal = RegisterFile.getValue(16); // Store score
                                    RegisterFile.updateRegister(16, scoreVal + 5); // Increase score by 5

                                    SystemIO.printString("Tetris! Score: +5\n");
                                }
                            }
                        }));

        // ghostpiece
        instructionList.add(
                new BasicInstruction("ghostpiece $v0, $t1, -100",
                        "Preview what a register value would be if you were to add a certain value to it ($v0 = $t0 + imm). Store in $v0",
                        BasicInstructionFormat.I_FORMAT,
                        "111010 ttttt sssss iiiiiiiiiiiiiiii",
                        new SimulationCode()
                        {
                            public void simulate(ProgramStatement statement) throws ProcessingException
                            {
                                int[] operands = statement.getOperands();
                                int baseVal = RegisterFile.getValue(operands[1]);
                                int imm = operands[2] << 16 >> 16;

                                int sum = baseVal + imm;

                                SystemIO.printString("The value from adding " + imm + " to " + operands[0] + " would be " + sum + ".\n");
                                RegisterFile.updateRegister(operands[0], sum); // Don't actually update $t register, just store in $v
                            }
                        }));

        // drop
        instructionList.add(
                new BasicInstruction("drop $t0",
                        "Add one to four (random) to chosen registers within $t0 to $t9 ($t0 += imm)",
                        BasicInstructionFormat.R_FORMAT,
                        "011101 00000 00000 fffff 00000 000001",
                        new SimulationCode()
                        {
                            public void simulate(ProgramStatement statement) throws ProcessingException
                            {
                                int[] operands = statement.getOperands();
                                int baseVal = RegisterFile.getValue(operands[0]);

                                Random rand = new Random();
                                int randVal = rand.nextInt(4) + 1; // Roll number from one to four

                                RegisterFile.updateRegister(operands[0], baseVal + randVal);
                            }
                        }));

        // rotate
        instructionList.add(
                new BasicInstruction("rotate $t0, $t1, $t2",
                        "Swap register values; $t0 <-> $t1",
                        BasicInstructionFormat.R_FORMAT,
                        "011101 sssss ttttt fffff 00000 000010",
                        new SimulationCode()
                        {
                            public void simulate(ProgramStatement statement) throws ProcessingException
                            {
                                int[] operands = statement.getOperands();
                                int tempReg0 = RegisterFile.getValue(operands[0]); // Store register values to swap
                                int tempReg1 = RegisterFile.getValue(operands[1]);

                                RegisterFile.updateRegister(operands[0], tempReg1);
                                RegisterFile.updateRegister(operands[1], tempReg0);
                            }
                        }));

        // slowgravity
        instructionList.add(
                new BasicInstruction("slowgravity $t0, $t1, -100",
                        "Subtract immediate value from register; $t0 = $t1 - imm",
                        BasicInstructionFormat.I_FORMAT,
                        "111011 ttttt sssss iiiiiiiiiiiiiiii",
                        new SimulationCode() {
                            public void simulate(ProgramStatement statement) throws ProcessingException {
                                int[] operands = statement.getOperands();
                                int baseVal = RegisterFile.getValue(operands[1]);
                                int value = operands[2] << 16 >> 16;
                                int result = baseVal - value;

                                RegisterFile.updateRegister(operands[0], result);
                            }
                        }));

        // combo
        instructionList.add(
                new BasicInstruction("combo -100",
                        "Add chosen value to all registers $t0 to $t9",
                        BasicInstructionFormat.I_FORMAT,
                        "111100 00000 00000 iiiiiiiiiiiiiiii",
                        new SimulationCode()
                        {
                            public void simulate(ProgramStatement statement) throws ProcessingException
                            {
                                int[] operands = statement.getOperands();
                                int imm = operands[0] << 16 >> 16;

                                for (int i = 8; i < 16; i++) { // $t0 to $t7
                                    int baseVal = RegisterFile.getValue(i);
                                    RegisterFile.updateRegister(i, baseVal + imm);
                                }
                                for (int i = 24; i < 26; i++) { // $t8 to $t9
                                    int baseVal = RegisterFile.getValue(i);
                                    RegisterFile.updateRegister(i, baseVal + imm);
                                }
                            }
                        }));
    }
}

// To print, just use SystemIO.printString()
// $t0 to $t7 is 8-15, $t8-$t9 is 24-25
// $v0 is 2
// $s0 is 16

