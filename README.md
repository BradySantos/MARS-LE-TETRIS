# README

# Instructions:

**Basic Instructions:**

harddrop (I)
harddrop rt, rs, imm

attack (R)
attack rd, rs, rt

delay (R)
delay rd, rs, rt

stack (R)
stack rd, rs, rt

garbage (R)
garbage rd, rs, rt

hold (I)
hold rt, imm(rs)

next (I)
next rt, imm(rs)

shift (J)
shift label

lineclear (I)
lineclear rs, rt, label

nonclear (I)
nonclear rs, rt, label


**Unique Instructions:**

downstack (R)
downstack

topout (R)
topout

allclear (R)
allclear

viewboard (R)
viewboard

tetris (R)
tetris

ghostpiece (I)
ghostpiece rs, imm

drop (R) 
drop rt

rotate (R)
rotate rd, rs

slowgravity (I)
slowgravity rt, rs, imm

combo (I)
combo imm

# How to run:

To run my custom MIPS language, download the MARS LE zip file, making sure the Tetris language is within the customlangs folder (directory: \MARS-LE-main\MARS-LE-main\mars\mips\instructions\customlangs). Then open your computer’s terminal and cd to the MARS LE directory: \MARS-LE-main\MARS-LE-main. Type java -jar BuildCustomLang.jar Tetris.java, and this should add the custom language to Mars LE when you open it. To switch languages, simply click Tools → Language Switcher. Clicking the question mark provides an overview of the Tetris language.


# Example Programs

_Basic Math Instructions_


_Sum of Even Numbers_


harddrop $t0, $zero, 0 # addi or li

harddrop $t1, $zero, 100

harddrop $t2, $zero, 0

Loop:

harddrop $t0, $t0, 2

stack $t2, $t0, $t2 # add

nonclear $t0, $t1, Loop # bne


_Other Math Instructions_


harddrop $t3, $zero, 2

harddrop $t4, $zero, 5

attack $t5, $t3, $t4 # mul

delay $t4, $t3 # div

garbage $t7, $t4, $t3 #sub


_Memory Instructions_

_Memory accessing_


harddrop $t0, $zero, 20 # addi

harddrop $t1, $zero, 10010000

hold $t0, 0($t1) # sw

next $t3, 0($t0) # lw

shift Test # j

Test:

lineclear $t0, $t2, End # beq

shift Test

End:


_Unique Instructions_

# Tetris (Note: ghostpiece, combo, slowgravity should be commented out before running as they are not fully implemented)

harddrop $t0, $zero, 1 # Add 1 to all $t registers

harddrop $t1, $zero, 1

harddrop $t2, $zero, 1

harddrop $t3, $zero, 1

harddrop $t4, $zero, 1

harddrop $t5, $zero, 1

harddrop $t6, $zero, 1

harddrop $t7, $zero, 1

harddrop $t8, $zero, 1

harddrop $t9, $zero, 1

viewboard # View all $t register values

downstack # Subtract 1 from all $t registers and increase score in $s0 by 1

allclear # If all $t registers are 0, double score in $s0

harddrop $t0, $zero, 19

drop $t0 # Add random num 1 to 4 to chosen register

topout # If any $t registers have values 20 or greater, set $t registers to 0, $s0 to 0, print final score

ghostpiece $v0, $t1, 10 # Preview what the register value would be if you were to add 10; value is stored in $v0

combo 6 # Add 6 to all $t registers

viewboard 

tetris # If all values in $t registers are at least 4, subtract 4 from each and increase $s0 by 5. Print "Tetris!"

slowgravity $t3, $t2, 1 # Subtract immediate value 1 from $t2; store in $t3

rotate $t3, $t4, $zero # Swap values in $t3 and $t4






