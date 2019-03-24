/*
 * Part of Factorio Assembler
 * Copyright (c) 2019 - 2019 Alex O'Neill
 * See the project LICENCE.md for more information
 */

package assembler.util;

public class InvalidAssemblyException extends Exception
{
    public InvalidAssemblyException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public InvalidAssemblyException(String message)
    {
        super(message);
    }
}
