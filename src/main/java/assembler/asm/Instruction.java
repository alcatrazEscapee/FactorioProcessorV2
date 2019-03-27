/*
 * Part of Factorio Assembler
 * Copyright (c) 2019 - 2019 Alex O'Neill
 * See the project LICENCE.md for more information
 */

package assembler.asm;

import java.util.function.IntUnaryOperator;

import assembler.util.InvalidAssemblyException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Instruction implements IInstruction
{
    private final InstructionType type;
    private final int line;
    private final String text;
    private int encoded;
    private String symbol;
    private IntUnaryOperator symbolOperator;

    Instruction(@NotNull InstructionType type, int line, String text, @NotNull String[] args) throws InvalidAssemblyException
    {
        this.line = line;
        this.type = type;
        this.text = text;
        this.encoded = parseArguments(args) | type.opcode;
    }

    @Override
    public int getEncoded()
    {
        return encoded;
    }

    @Override
    public int getLine()
    {
        return line;
    }

    @Nullable
    public String getSymbol()
    {
        return symbol;
    }

    @Override
    public void setSymbol(int symbolLine)
    {
        if (symbol != null)
        {
            symbol = null;
            // Apply the operator (if it isn't null)
            if (symbolOperator != null)
            {
                symbolLine = symbolOperator.applyAsInt(symbolLine);
            }
            switch (type.type)
            {
                case 1: // Call = 11 bit signed offset value
                    encoded |= (((symbolLine - line - 1) & 0b11111111111) << 5);
                    break;
                case 4: // Branch = 6 bit signed offset value
                    encoded |= (((symbolLine - line - 1) & 0b111111) << 4);
                    break;
                case 5: // ALU Instructions = 6 bit signed immediate value
                case 6:
                    encoded |= ((symbolLine) << 4);
            }
        }
    }

    @Override
    public String toString()
    {
        return "[" + type.name().toLowerCase() + "] " + text;
    }

    private int parseArguments(@NotNull String[] args) throws InvalidAssemblyException
    {
        switch (type.type)
        {
            case -1:
                return 0; // Special instructions that have a 16-bit opcode for async program control
            case 1:
                if (type == InstructionType.CALL) this.symbol = args[0];
                return 0;
            case 2:
                return (reg(args[0]) << 13) | (reg(args[1]) << 10) | (reg(args[2]) << 7);
            case 3:
                String[] slArgs = args[1].replace('(', ',').split(",");
                return (reg(args[0]) << 13) | (reg(slArgs[1].substring(0, slArgs[1].length() - 1)) << 10) | (imm6Sign(slArgs[0]) << 4);
            case 4:
            case 5:
                return (reg(args[0]) << 13) | (reg(args[1]) << 10) | (imm6Sign(args[2]) << 4);
            case 6:
                return (reg(args[0]) << 13) | (reg(args[1]) << 10) | (imm6Logical(args[2]) << 4);
        }
        throw new InvalidAssemblyException("Invalid Instruction Type");
    }

    private int imm6Sign(@NotNull String arg) throws InvalidAssemblyException
    {
        boolean negative = false;
        if (arg.startsWith("-"))
        {
            // Negative
            negative = true;
            arg = arg.substring(1);
        }
        int result;
        try
        {
            result = Integer.parseInt(arg);
        }
        catch (NumberFormatException e)
        {
            // Not a number, so assume its a symbol
            // Check if it has a compiler flag for reduced bit fields
            parseSymbol(arg);
            return 0;
        }
        if (negative)
        {
            result = ~result + 1;
        }
        if ((negative && (result & 0b100000) == 0) || (!negative && (result & 0b100000) != 0))
        {
            throw new InvalidAssemblyException("Immediate larger than 6-bits");
        }
        return result & 0b111111;
    }

    private int imm6Logical(@NotNull String arg) throws InvalidAssemblyException
    {
        int result;
        try
        {
            result = Integer.parseInt(arg);
        }
        catch (NumberFormatException e)
        {
            // Not a number, so assume its a symbol
            // Check if it has a compiler flag for reduced bit fields
            parseSymbol(arg);
            return 0;
        }
        if ((result & 0b111111) != result)
        {
            throw new InvalidAssemblyException("Logical Immediate larger than 6-bits");
        }
        return result;
    }

    private int reg(@NotNull String arg) throws InvalidAssemblyException
    {
        if ("ra".equals(arg))
        {
            return 1;
        }
        if ("sp".equals(arg))
        {
            return 7;
        }
        int result = Integer.parseInt(arg.substring(1));
        if (result < 0 || result > 7)
        {
            throw new InvalidAssemblyException("Invalid Register");
        }
        return result;
    }

    private void parseSymbol(String symbol)
    {
        if (symbol.startsWith("[5-0]"))
        {
            this.symbolOperator = i -> (i & 0b111111);
            symbol = symbol.substring(5);
        }
        else if (symbol.startsWith("[11-6]"))
        {
            this.symbolOperator = i -> ((i >> 6) & 0b111111);
            symbol = symbol.substring(6);
        }
        else
        {
            this.symbolOperator = i -> i;
        }
        this.symbol = symbol;
    }

}
