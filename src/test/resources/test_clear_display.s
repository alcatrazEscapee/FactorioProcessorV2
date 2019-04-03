    movi r2, 5
    lsi r2, r2, 6 # add memory address for io
    movi r5, 16 # counter for display
loop:
    stw r0, 0(r2)
    subi r5, r5, 1
    addi r2, r2, 1
    bgt r5, r0, loop
    exit