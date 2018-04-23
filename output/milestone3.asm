.text
main:
    ADDI $11, $zero, 9
    SUB $sp, $sp, 8
    SW $v0, 0($sp)
    SW $a0, 4($sp)
    ADDI $a0, $zero, 0
    JAL _new_object
    ADD $10, $v0, $zero
    LW $v0, 0($sp)
    LW $a0, 4($sp)
    ADD $sp, $sp, 8
    
    
    SUB $sp, $sp, 12
    SW $v0, 0($sp)
    SW $5, 4($sp)
    SW $4, 8($sp)
    ADD $4, $zero, $10
    ADD $5, $zero, $11
    JAL Test2__Start
    ADD $16, $v0, $zero
    LW $v0, 0($sp)
    LW $5, 4($sp)
    LW $4, 8($sp)
    ADDI $sp, $sp, 12

    ADD $a0, $zero, $16
    JAL _system_out_println
    J _system_exit


Test2__Start:
    SUB $sp, $sp, 4
    SW $ra, 0($sp)
    
    ADD $v0, $5, $zero
    LW $ra, 0($sp)
    ADD $sp, $sp, 4
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

