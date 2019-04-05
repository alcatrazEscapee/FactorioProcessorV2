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
    private final List<IInstruction> lines;
    private final Map<String, Integer> symbolTable;
    private int currentLine;
    private int currentMemoryLoc;

    public Assembly()
    {
        this.lines = new ArrayList<>();
        this.symbolTable = new HashMap<>();
        this.currentLine = 0;
        this.currentMemoryLoc = 0;

        // Init basic symbols
        symbolTable.put("LAST_RAM_LOC", 319);
        symbolTable.put("CHAR_OUT_LOC", 320);
        symbolTable.put("CHAR_IN_LOC", 336);
    }

    public void addData(@NotNull String dataString)
    {
        addData(dataString.getBytes(StandardCharsets.US_ASCII));
        addData((byte) 0);
    }

    public void addMemory(@NotNull String[] args) throws InvalidAssemblyException
    {
        int amount = Integer.valueOf(args[1]);
        if (amount + currentMemoryLoc > 64)
        {
            throw new InvalidAssemblyException("Memory overflow! Too much memory allocated.");
        }
        if (amount <= 0)
        {
            throw new InvalidAssemblyException("Can't allocate a non-positive amount of memory.");
        }
        // Add the label for the memory location
        symbolTable.put(args[0], currentMemoryLoc + 256);
        // Increment the current memory counter
        currentMemoryLoc += amount;
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

    @Override
    public String toString()
    {
        return String.format("Lines:\n%s\nSymbols:\n%s\nMemory: %d / 64\n", lines.stream().map(x -> String.format("%3d | %16s | %5d | %s", x.getLine(), x.getEncodedString(), x.getEncoded(), x.toString())).collect(Collectors.joining("\n")), symbolTable, currentMemoryLoc);
    }

    public void addLabel(@NotNull String label)
    {
        symbolTable.put(label, currentLine);
    }

    private void addData(@NotNull byte... dataValues)
    {
        for (int val : dataValues)
        {
            lines.add(new InstructionRaw(val, currentLine, "asciz"));
            currentLine++;
        }
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

    private void addInstruction(@NotNull IInstruction instruction)
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
}
