/*
 * Part of Factorio Assembler
 * Copyright (c) 2019 - 2019 Alex O'Neill
 * See the project LICENCE.md for more information
 */

package assembler.code;

import assembler.util.InvalidAssemblyException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Instruction implements IInstruction
{
    private final InstructionType type;
    private int encoded;
    private String symbol;

    public Instruction(@NotNull InstructionType type, @NotNull String[] args) throws InvalidAssemblyException
    {
        this.type = type;
        this.encoded = parseArguments(args) | type.opcode;
    }

    @Override
    public int getEncoded()
    {
        return encoded;
    }

    @Nullable
    public String getSymbol()
    {
        return symbol;
    }

    @Override
    public void setSymbol(int symbolValue)
    {
        if (symbol != null)
        {
            symbol = null;
            switch (type.type)
            {
                case 1: // Call = 11 bit signed offset value
                    encoded |= ((symbolValue & 0b11111111111) << 5);
                    break;
                case 4: // Branch = 6 bit signed offset value
                    encoded |= ((symbolValue & 0b111111) << 4);
                    break;
            }
        }
    }

    @Override
    public String toString()
    {
        return type.name().toLowerCase();
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
                return (reg(args[0]) << 13) | (reg(slArgs[1].substring(0, slArgs[1].length() - 1)) << 10) | (imm6(slArgs[0]) << 4);
            case 4:
            case 5:
            case 6:
                return (reg(args[0]) << 13) | (reg(args[1]) << 10) | (imm6(args[2]) << 4);
        }
        throw new InvalidAssemblyException("error.invalid_instruction_type");
    }

    private int imm6(@NotNull String arg) throws InvalidAssemblyException
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
            this.symbol = arg;
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

    private int reg(@NotNull String arg) throws InvalidAssemblyException
    {
        if ("ra".equals(arg))
        {
            return 1;
        }
        int result = Integer.parseInt(arg.substring(1));
        if (result < 0 || result > 7)
        {
            throw new InvalidAssemblyException("Invalid Register");
        }
        return result;
    }

}
