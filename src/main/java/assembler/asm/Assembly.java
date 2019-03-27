/*
 * Part of Factorio Assembler
 * Copyright (c) 2019 - 2019 Alex O'Neill
 * See the project LICENCE.md for more information
 */

package assembler.asm;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
            lines.add(new InstructionRaw(val, currentLine, "asciz"));
            currentLine++;
        }
    }

    public void addInstruction(@NotNull String name, @NotNull String text, @NotNull String[] args) throws InvalidAssemblyException
    {
        InstructionType type = InstructionType.get(name);
        if (type == null)
        {
            throw new InvalidAssemblyException("Unknown Instruction Type");
        }
        Instruction inst = new Instruction(type, currentLine, text, args);
        addInstruction(inst);
    }

    public void addExit()
    {
        addInstruction(new InstructionRaw(0b1000000000010000, currentLine, "exit"));
    }

    public void addInstruction(@NotNull IInstruction instruction)
    {
        String symbol = instruction.getSymbol();
        if (symbol != null)
        {
            int value = symbolTable.getOrDefault(symbol, -1);
            if (value != -1)
            {
                instruction.setSymbol(value);
            }
        }

        lines.add(instruction);
        currentLine++;
    }

    public void addLabel(@NotNull String label)
    {
        symbolTable.put(label, currentLine);
    }

    public void applyLinker() throws InvalidAssemblyException
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
                    inst.setSymbol(value);
                }
                else
                {
                    InvalidAssemblyException e = new InvalidAssemblyException("Unknown symbol " + symbol);
                    e.attachData("Current Line: " + i, "Instruction: " + inst, this);
                    throw e;
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

    @Override
    public String toString()
    {
        return String.format("Name: %s\nLines:\n%s\nSymbols:\n%s\n", name, lines.stream().map(x -> String.format("%3d | %16s | %5d | %s", x.getLine(), x.getEncodedString(), x.getEncoded(), x.toString())).collect(Collectors.joining("\n")), symbolTable);
    }
}
