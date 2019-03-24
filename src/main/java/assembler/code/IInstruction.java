/*
 * Part of Factorio Assembler
 * Copyright (c) 2019 - 2019 Alex O'Neill
 * See the project LICENCE.md for more information
 */

package assembler.code;

import org.jetbrains.annotations.Nullable;

public interface IInstruction
{
    int getEncoded();

    @Nullable
    default String getSymbol()
    {
        return null;
    }

    default void setSymbol(int symbolValue) {}

    default String getEncodedString()
    {
        return String.format("%16s", Integer.toBinaryString(getEncoded())).replace(' ', '0');
    }
}
