/*
 * Part of Factorio Assembler
 * Copyright (c) 2019 - 2019 Alex O'Neill
 * See the project LICENCE.md for more information
 */

package assembler.code;

public class InstructionData implements IInstruction
{
    public static InstructionData exit()
    {
        return new InstructionData(0b1000000000010000, "exit");
    }

    private final int data;
    private final String name;

    InstructionData(int data)
    {
        this(data, "asciz data");
    }

    private InstructionData(int data, String name)
    {
        this.data = data;
        this.name = name;
    }

    @Override
    public int getEncoded()
    {
        return data;
    }

    @Override
    public String toString()
    {
        return name;
    }
}
