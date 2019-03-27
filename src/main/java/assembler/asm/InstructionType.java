/*
 * Part of Factorio Assembler
 * Copyright (c) 2019 - 2019 Alex O'Neill
 * See the project LICENCE.md for more information
 */

package assembler.asm;

import java.util.HashMap;
import java.util.Set;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum InstructionType
{
    CALL("call", 0b00000, 1, 1),
    RETURN("ret", 0b10000, 2, 1),
    ADD("add", 0b0000001, 3, 2),
    SUB("sub", 0b0010001, 4, 2),
    MUL("mul", 0b0100001, 5, 2),
    DIV("div", 0b0110001, 6, 2),
    POW("pow", 0b1000001, 7, 2),
    MOD("mod", 0b1010001, 8, 2),
    AND("and", 0b1100001, 9, 2),
    OR("or", 0b1110001, 10, 2),
    XOR("xor", 0b0000010, 11, 2),
    XNOR("xnor", 0b0010010, 12, 2),
    LEFT_SHIFT("ls", 0b0100010, 13, 2),
    RIGHT_SHIFT("rs", 0b0110010, 14, 2),
    LEFT_ROTATE("lr", 0b1000010, 15, 2),
    RIGHT_ROTATE("rr", 0b1010010, 16, 2),
    STORE("stw", 0b0011, 19, 3),
    LOAD("ldw", 0b0100, 20, 3),
    BR_EQUAL("beq", 0b0101, 21, 4),
    BR_NOT_EQUAL("bne", 0b0110, 22, 4),
    BR_GREATER("bgt", 0b0111, 23, 4),
    BR_LESS("blt", 0b1000, 24, 4),
    ADDI("addi", 0b1001, 25, 5),
    MULI("muli", 0b1010, 26, 5),
    DIVI("divi", 0b1011, 27, 5),
    ANDI("andi", 0b1100, 28, 6),
    ORI("ori", 0b1101, 29, 6),
    LEFT_SHIFTI("lsi", 0b1110, 30, 6),
    RIGHT_SHIFTI("rsi", 0b1111, 31, 6),
    BREAK("break", 0b1111111111110000, -1, -1),
    EXIT("exit", 0b0111111111110000, -1, -1);

    private static final HashMap<String, InstructionType> TYPES;

    static
    {
        TYPES = new HashMap<>();
        for (InstructionType value : InstructionType.values())
        {
            TYPES.put(value.name, value);
        }
    }

    @NotNull
    @Contract(pure = true)
    public static Set<String> keys()
    {
        return TYPES.keySet();
    }

    @Nullable
    @Contract(pure = true)
    public static InstructionType get(String name)
    {
        return TYPES.get(name);
    }

    final int opcode, id, type;
    private final String name;

    InstructionType(String name, int opcode, int id, int type)
    {
        this.name = name;
        this.opcode = opcode;
        this.id = id;
        this.type = type;
    }
}
