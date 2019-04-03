    movi r2, 5
    lsi r2, r2, 6 # add memory address for io
    movi r4, 18 # add memory address for text
    ldw r3, 0(r4)
loop_hello:
    stw r3, 0(r2) # send character to display
    addi r4, r4, 1 # increment character pointer
    addi r2, r2, 1 # increment the io pointer
    ldw r3, 0(r4)
    bne r3, r0, loop_hello # keep looping while there are more characters
    addi r2, r2, 3 # move the io pointer to the next line
    addi r4, r4, 1 # move the character pointer to the next word
    ldw r3, 0(r4)
loop_world:
    stw r3, 0(r2) # send character to display
    addi r4, r4, 1 # increment character pointer
    addi r2, r2, 1 # increment io pointer
    ldw r3, 0(r4)
    bne r3, r0, loop_world # keep looping while there are more characters
    exit

    .asciz hello
    .asciz world!
