/*
 * Part of Factorio Assembler
 * Copyright (c) 2019 - 2019 Alex O'Neill
 * See the project LICENCE.md for more information
 */

package assembler.asm;

public class InstructionRaw implements IInstruction
{
    private final int data;
    private final int line;
    private final String name;

    public InstructionRaw(int data, int line, String name)
    {
        this.data = data;
        this.line = line;
        this.name = name;
    }

    @Override
    public int getEncoded()
    {
        return data;
    }

    @Override
    public int getLine()
    {
        return line;
    }

    @Override
    public String toString()
    {
        return "[" + name + "]";
    }
}
