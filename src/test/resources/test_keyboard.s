# Test of keyboard interaction
    movia   r4, 320         # display base memory location
    movia   r3, 336         # keyboard output memory location
loop:
    ldw     r2, 0(r3)
    beq     r2, r0, loop    # read the key, and while zero, loop
    stw     r2, 0(r4)       # store the key press
    exit
