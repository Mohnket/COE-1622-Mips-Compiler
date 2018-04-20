.text
main:
    ADDI $8, $zero, 9
    ADD $9, $zero, $zero
    ADD $4, $9, $zero
    ADD $5, $8, $zero
    JAL Test2__Start
    ADD $10, $v0, $zero
    ADD $a0, $zero, $10
    JAL _system_out_println
    J _system_exit


Test2__Start:
    SUB $sp, $sp, 76
    SW $8, 0($sp)
    SW $9, 4($sp)
    SW $10, 8($sp)
    SW $11, 12($sp)
    SW $12, 16($sp)
    SW $13, 20($sp)
    SW $14, 24($sp)
    SW $15, 28($sp)
    SW $16, 32($sp)
    SW $17, 36($sp)
    SW $18, 40($sp)
    SW $19, 44($sp)
    SW $20, 48($sp)
    SW $21, 52($sp)
    SW $22, 56($sp)
    SW $23, 60($sp)
    SW $24, 64($sp)
    SW $25, 68($sp)
    SW $ra, 72($sp)
    ADD $8, $5, $zero
    ADD $8, $8, $zero
    ADD $v0, $8, $zero
    LW $8, 0($sp)
    LW $9, 4($sp)
    LW $10, 8($sp)
    LW $11, 12($sp)
    LW $12, 16($sp)
    LW $13, 20($sp)
    LW $14, 24($sp)
    LW $15, 28($sp)
    LW $16, 32($sp)
    LW $17, 36($sp)
    LW $18, 40($sp)
    LW $19, 44($sp)
    LW $20, 48($sp)
    LW $21, 52($sp)
    LW $22, 56($sp)
    LW $23, 60($sp)
    LW $24, 64($sp)
    LW $25, 68($sp)
    LW $ra, 72($sp)
    ADD $sp, $sp, 76
    JR $ra


# main is testing the functions I've provided. You will include this code at the end
# of your output file so that you may call these system services.

#main:
#	li $a0, 100
#	jal _new_array
#	move $s0, $v0
#	move $a0, $v0
#	jal _system_out_println
#	lw $a0, 0($s0)
#	jal _system_out_println
#	jal _system_exit

_system_exit:
	li $v0, 10 #exit
	syscall
	
# Integer to print is in $a0. 
# Kills $v0 and $a0
_system_out_println:
	# print integer
	li  $v0, 1 
	syscall
	# print a newline
	li $a0, 10
	li $v0, 11
	syscall
	jr $ra
	
# $a0 = number of bytes to allocate
# $v0 contains address of allocated memory
_new_object:
	# sbrk
	li $v0, 9 
	syscall
	
	#initialize with zeros
	move $t0, $a0
	move $t1, $v0
_new_object_loop:
	beq $t0, $zero, _new_object_exit
	sb $zero, 0($t1)
	addi $t1, $t1, 1
	addi $t0, $t0, -1
	j _new_object_loop
_new_object_exit:
	jr $ra
	
# $a0 = number of bytes to allocate 
# $v0 contains address of allocated memory (with offset 0 being the size)	
_new_array:
	# add space for the size (1 integer)
	addi $a0, $a0, 4
	# sbrk
	li $v0, 9
	syscall
#initialize to zeros
	move $t0, $a0
	move $t1, $v0
_new_array_loop:
	beq $t0, $zero, _new_array_exit
	sb $zero, 0($t1)
	addi $t1, $t1, 1
	addi $t0, $t0, -1
	j _new_array_loop
_new_array_exit:
	#store the size (number of ints) in offset 0
	addi $t0, $a0, -4
	sra $t0, $t0, 2
	sw $t0, 0($v0)
	jr $ra

