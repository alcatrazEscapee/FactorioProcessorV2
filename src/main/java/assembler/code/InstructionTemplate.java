/*
 * Part of Factorio Assembler
 * Copyright (c) 2019 - 2019 Alex O'Neill
 * See the project LICENCE.md for more information
 */

package assembler.code;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public enum InstructionTemplate
{
    MOV("mov"),
    MOVI("movi"),
    MOVI_LONG("movil"),
    SUBI("subi");

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
            case MOVI_LONG:
                // todo: all of this
                throw new UnsupportedOperationException("Not implemented yet!");
            case SUBI:
                return List.of("addi " + args[0] + "," + args[1] + ",-" + args[2]);
            default:
                throw new UnsupportedOperationException("Not implemented!");
        }
    }
}
