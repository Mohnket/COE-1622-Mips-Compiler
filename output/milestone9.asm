.text
main:
    SUB $sp, $sp, 8
    SW $v0, 0($sp)
    SW $a0, 4($sp)
    ADDI $a0, $zero, 12
    JAL _new_object
    ADD $10, $v0, $zero
    LW $v0, 0($sp)
    LW $a0, 4($sp)
    ADD $sp, $sp, 8
    
    SUB $sp, $sp, 8
    SW $v0, 0($sp)
    SW $4, 4($sp)
    ADD $4, $zero, $10
    JAL C__test
    ADD $16, $v0, $zero
    LW $v0, 0($sp)
    LW $4, 4($sp)
    ADDI $sp, $sp, 8

    ADD $a0, $zero, $16
    JAL _system_out_println
    J _system_exit


B__bTest:
    SUB $sp, $sp, 12
    SW $16, 0($sp)
    SW $17, 4($sp)
    SW $ra, 8($sp)
    ADDI $8, $zero, 3
    ADD $16, $8, $zero
    
    
    
    SUB $sp, $sp, 8
    SW $v0, 0($sp)
    SW $4, 4($sp)
    ADD $4, $zero, $4
    JAL A__aTest
    ADD $17, $v0, $zero
    LW $v0, 0($sp)
    LW $4, 4($sp)
    ADDI $sp, $sp, 8

    ADD $2, $16, $17
    ADD $v0, $2, $zero
    LW $16, 0($sp)
    LW $17, 4($sp)
    LW $ra, 8($sp)
    ADD $sp, $sp, 12
    JR $ra


A__aTest:
    SUB $sp, $sp, 4
    SW $ra, 0($sp)
    ADDI $8, $zero, 2
    ADD $2, $8, $zero
    
    ADD $v0, $2, $zero
    LW $ra, 0($sp)
    ADD $sp, $sp, 4
    JR $ra


C__test:
    SUB $sp, $sp, 12
    SW $16, 0($sp)
    SW $17, 4($sp)
    SW $ra, 8($sp)
    ADDI $8, $zero, 4
    ADD $16, $8, $zero
    
    
    
    SUB $sp, $sp, 8
    SW $v0, 0($sp)
    SW $4, 4($sp)
    ADD $4, $zero, $4
    JAL B__bTest
    ADD $17, $v0, $zero
    LW $v0, 0($sp)
    LW $4, 4($sp)
    ADDI $sp, $sp, 8

    ADD $2, $16, $17
    ADD $v0, $2, $zero
    LW $16, 0($sp)
    LW $17, 4($sp)
    LW $ra, 8($sp)
    ADD $sp, $sp, 12
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

