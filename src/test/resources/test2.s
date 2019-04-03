# Conditional branching, using offset memory addresses and labels
        movi    r3, 5
START:  addi    r2, r2, 1
        blt     r2, r3, START
MIDDLE:
        subi    r2, r2, 1
        bgt     r2, r3, MIDDLE
END:    exit