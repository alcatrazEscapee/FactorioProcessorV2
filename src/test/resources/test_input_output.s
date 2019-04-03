# Main Program
    # This program makes use of assembler labels
    # These labels are configured to be the appropriate memory location for various areas in memory, or io devices
    movia   sp, LAST_RAM_LOC# init stack pointer to the last RAM location

    movia   r3, MSG1        # prompt string pointer
    movia   r4, CHAR_OUT_LOC# display memory address
    call    PrintString     # print 'Name?'

    movia   r3, USER_INPUT  # location of string in RAM
    addi    r4, r4, 8       # next line
    call    GetString       # get string from user

    call    ClearDisplay    # reset display

    movia   r3, MSG2        # prompt string pointer
    movia   r4, CHAR_OUT_LOC# display memory address
    call    PrintString     # print 'Hello,'

    movia   r3, USER_INPUT  # location of string in RAM
    addi    r4, r4, 8       # next line
    call    PrintString     # print string back to user
    exit

# Subroutine to get string input from user
# r3 = location for the string in RAM
# r4 = display location to start printing string to
GetString:
    subi    sp, sp, 2       # save initial pointer values
    stw     r3, 0(sp)
    stw     r4, 1(sp)

    movia   r5, CHAR_IN_LOC # keyboard buffer
    movi    r6, 10          # '\n' ASCII code
gs_loop:
    ldw     r2, 0(r5)       # read the key buffer
    beq     r2, r0, gs_loop # while the key is zero, loop
    beq     r2, r6, gs_end  # if '\n', exit the loop
    stw     r2, 0(r3)       # store key in memory
    stw     r2, 0(r4)       # print key to display
    addi    r3, r3, 1       # increment string pointer
    addi    r4, r4, 1       # increment display pointer
    br      gs_loop
gs_end:
    addi    r3, r3, 1       # store a '\0'
    stw     r0, 0(r3)

    ldw     r3, 0(sp)       # restore initial pointer values
    ldw     r4, 1(sp)
    addi    sp, sp, 2
    ret                     # return

# Subroutine. Prints a string to the output
# r3 = pointer to a string
# r4 = memory output pointer
PrintString:
    subi    sp, sp, 2       # stack positions for 3 words
    stw     r3, 1(sp)       # store r3
    stw     r4, 2(sp)       # store r4

    ldw     r2, 0(r3)       # load the first character
ps_loop:
    stw     r2, 0(r4)       # send character to output display
    addi    r3, r3, 1       # increment string pointer
    addi    r4, r4, 1       # increment memory address
    ldw     r2, 0(r3)       # load the next character
    bne     r2, r0, ps_loop # break while not found a null character

    ldw     r3, 1(sp)       # load original r3
    ldw     r4, 2(sp)       # load original r4
    addi    sp, sp, 2       # un-reserve space in stack
    ret                     # exit the subroutine

# Subroutine. Clears the display
ClearDisplay:
    movia   r2, CHAR_OUT_LOC# display memory address
    movi    r5, 16          # counter
loop:
    stw     r0, 0(r2)
    subi    r5, r5, 1
    addi    r2, r2, 1
    bgt     r5, r0, loop
    ret

# Constant Strings (these are saved to ROM)
MSG1:
    .asciz Name?
MSG2:
    .asciz Hello,

# RAM Stuff (this allocates sections of RAM for the program execution, and adds the relevant labels)
    .malloc USER_INPUT, 8
