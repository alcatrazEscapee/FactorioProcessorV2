/*
 * Part of Factorio Assembler
 * Copyright (c) 2019 - 2019 Alex O'Neill
 * See the project LICENCE.md for more information
 */

package assembler.util;

import java.util.Arrays;
import java.util.stream.Collectors;

public class InvalidAssemblyException extends Exception
{
    private String extraData = "";

    public InvalidAssemblyException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public InvalidAssemblyException(String message)
    {
        super(message);
    }

    public void attachData(Object... args)
    {
        extraData += Arrays.stream(args).map(Object::toString).collect(Collectors.joining("\n"));
    }

    public void printData()
    {
        System.out.println("Extra Debug Data:\n" + extraData);
    }
}
