/*
 * Part of Factorio Assembler
 * Copyright (c) 2019 - 2019 Alex O'Neill
 * See the project LICENCE.md for more information
 */

package assembler.code;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import assembler.util.InvalidAssemblyException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Assembly
{
    private final String name;
    private final List<IInstruction> lines;
    private final Map<String, Integer> symbolTable;
    private int currentLine;

    public Assembly(String name)
    {
        this.name = name;
        this.lines = new ArrayList<>();
        this.symbolTable = new HashMap<>();
        this.currentLine = 0;
    }

    public void addData(@NotNull String dataString)
    {
        addData(dataString.getBytes(StandardCharsets.US_ASCII));
        addData((byte) 0);
    }

    public void addData(@NotNull byte... dataValues)
    {
        for (int val : dataValues)
        {
            lines.add(new InstructionData(val));
            currentLine++;
        }
    }

    public void addInstruction(@NotNull String name, @NotNull String[] args) throws InvalidAssemblyException
    {
        InstructionType type = InstructionType.get(name);
        if (type == null)
        {
            throw new InvalidAssemblyException("Unknown Instruction Type");
        }
        Instruction inst = new Instruction(type, args);
        addInstruction(inst);
    }

    public void addInstruction(@NotNull IInstruction instruction)
    {
        String symbol = instruction.getSymbol();
        if (symbol != null)
        {
            int value = symbolTable.getOrDefault(symbol, -1);
            if (value != -1)
            {
                instruction.setSymbol(value - currentLine - 1);
            }
        }

        lines.add(instruction);
        currentLine++;
    }

    public void addLabel(@NotNull String label)
    {
        symbolTable.put(label, currentLine);
    }

    public void applyLinker()
    {
        for (int i = 0; i < lines.size(); i++)
        {
            IInstruction inst = lines.get(i);
            String symbol = inst.getSymbol();
            if (symbol != null)
            {
                // Calculate an offset value
                int value = symbolTable.getOrDefault(symbol, -1);
                if (value != -1)
                {
                    inst.setSymbol(i - value);
                }
            }
        }
    }

    @NotNull
    public String getName()
    {
        return name;
    }

    @NotNull
    public List<IInstruction> getInstructions()
    {
        return lines;
    }

    public int size()
    {
        return lines.size();
    }

    @Nullable
    public IInstruction getInstruction(int line)
    {
        return line >= lines.size() ? null : lines.get(line);
    }
}
