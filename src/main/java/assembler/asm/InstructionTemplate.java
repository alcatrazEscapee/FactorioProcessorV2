/*
 * Part of Factorio Assembler
 * Copyright (c) 2019 - 2019 Alex O'Neill
 * See the project LICENCE.md for more information
 */

package assembler.asm;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public enum InstructionTemplate
{
    MOV("mov"),
    MOVI("movi"),
    MOVI_UNSIGNED("moviu"),
    MOVI_LONG("movia"),
    SUBI("subi"),
    BREAK_GREATER_EQUAL("bge"),
    BREAK_LESS_EQUAL("ble"),
    BREAK_ZERO("brz"),
    BREAK_NOT_ZERO("bnz"),
    BREAK("br"),
    NOOP("nop");

    private static final HashMap<String, InstructionTemplate> TEMPLATES;

    static
    {
        TEMPLATES = new HashMap<>();
        for (InstructionTemplate value : InstructionTemplate.values())
        {
            TEMPLATES.put(value.name, value);
        }
    }

    @NotNull
    @Contract(pure = true)
    public static Set<String> keys()
    {
        return TEMPLATES.keySet();
    }

    @NotNull
    @Contract(pure = true)
    public static InstructionTemplate get(String name)
    {
        return TEMPLATES.get(name);
    }

    private final String name;

    InstructionTemplate(String name)
    {
        this.name = name;
    }

    @NotNull
    public List<String> convert(String[] args)
    {
        switch (this)
        {
            case MOV:
                return List.of("add " + args[0] + ",r0," + args[1]);
            case MOVI:
                return List.of("addi " + args[0] + ",r0," + args[1]);
            case MOVI_UNSIGNED:
                return List.of("ori " + args[0] + ",r0," + args[1]);
            case MOVI_LONG:
                try
                {
                    int result = Integer.parseInt(args[1]);
                    return List.of("ori " + args[0] + ",r0," + ((result >> 6) & 0b111111),
                            "lsi " + args[0] + "," + args[0] + ",6",
                            "ori " + args[0] + "," + args[0] + "," + (result & 0b111111));
                }
                catch (NumberFormatException e)
                {
                    return List.of("ori " + args[0] + ",r0,[11-6]" + args[1],
                            "lsi " + args[0] + "," + args[0] + ",6",
                            "ori " + args[0] + "," + args[0] + ",[5-0]" + args[1]);
                }
            case SUBI:
                return List.of("addi " + args[0] + "," + args[1] + ",-" + args[2]);
            case BREAK_GREATER_EQUAL:
                return List.of("blt " + args[1] + "," + args[0] + "," + args[2]);
            case BREAK_LESS_EQUAL:
                return List.of("bgt " + args[1] + "," + args[0] + "," + args[2]);
            case BREAK_ZERO:
                return List.of("beq " + args[0] + ",r0," + args[1]);
            case BREAK_NOT_ZERO:
                return List.of("bne " + args[0] + ",r0," + args[1]);
            case BREAK:
                return List.of("beq r0, r0, " + args[0]);
            case NOOP:
                return List.of("add r0, r0, r0");
            default:
                throw new UnsupportedOperationException("Not implemented!");
        }
    }
}
