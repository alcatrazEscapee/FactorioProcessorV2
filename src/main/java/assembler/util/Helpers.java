/*
 * Part of Factorio Assembler
 * Copyright (c) 2019 - 2019 Alex O'Neill
 * See the project LICENCE.md for more information
 */

package assembler.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Generic helper class with useful methods
 */
public final class Helpers
{
    @Nullable
    public static String loadFile(String fileName)
    {
        Path filePath = Paths.get(fileName);
        try (BufferedReader reader = Files.newBufferedReader(filePath))
        {
            return reader.lines().reduce((x, y) -> x + "\n" + y).orElse("");
        }
        catch (IOException e)
        {
            return null;
        }
    }

    @NotNull
    public static String loadResource(String fileName)
    {
        InputStream input = Helpers.class.getClassLoader().getResourceAsStream(fileName);
        if (input == null)
        {
            throw new Error("Resource '" + fileName + "' not found. This is a bug!");
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input)))
        {
            return reader.lines().reduce((x, y) -> x + "\n" + y).orElse("");
        }
        catch (IOException e)
        {
            throw new Error("Resource '" + fileName + "' not found. This is a bug!", e);
        }
    }

    public static List<String> getLinesUnformatted(String input)
    {
        return Arrays.stream(input
                .replaceAll("\r\n", "\n") // Standardize Line Endings
                .replaceAll("\r", "\n")
                .replaceAll("[ \t]+", " ") // Standardize spacing
                .split("\n"))
                .map(s -> s.split("#")[0]) // Trim comments
                .collect(Collectors.toList());
    }

    public static void nextChar(StringBuilder result, StringBuilder source)
    {
        char c = source.charAt(0);
        source.deleteCharAt(0);
        if (result.length() > 0 || c != ' ')
        {
            result.append(c);
        }
    }

    public static boolean isNextNonAlphanumeric(StringBuilder input)
    {
        return input.length() == 0 || !(Character.isLetterOrDigit(input.charAt(0)) || input.charAt(0) == '_');
    }

    /**
     * Rules for immediate values:
     * Decimal values can be signed with a negative prefix. Attempting to parse a negative string into a unsigned immediate will result in an error
     * Hex, Octal or Binary values must be positive. They are not sign-checked (so they may convert to negative numbers if the immediate is signed)
     *
     * @param input  The input string
     * @param length The length of the immediate value
     * @return the parsed immediate value
     * @throws InvalidAssemblyException if the immediate is out of the range given by the length and signed-ed-ness
     * @throws NumberFormatException    if the immediate is not parsable
     */
    public static int parseImmediate(String input, int length, boolean signed) throws InvalidAssemblyException
    {
        if (input.startsWith("0b"))
        {
            // Binary
            return parseImmediateUnsigned(input.substring(2), length, 2);
        }
        else if (input.startsWith("0x"))
        {
            // Hexadecimal
            return parseImmediateUnsigned(input.substring(2), length, 16);
        }
        else if (input.startsWith("0") && input.length() > 1)
        {
            // Octal
            return parseImmediateUnsigned(input.substring(1), length, 8);
        }
        else
        {
            // Decimal
            return parseImmediateSigned(input, length, signed);
        }
    }

    private static int parseImmediateUnsigned(String input, int length, int radix) throws InvalidAssemblyException
    {
        int value = Integer.parseInt(input, radix);
        int maxBits = (1 << length) - 1;
        if ((value & maxBits) != value)
        {
            throw new InvalidAssemblyException("Immediate out of range for " + length + "bits unsigned.");
        }
        return value;
    }

    private static int parseImmediateSigned(String input, int length, boolean signed) throws InvalidAssemblyException
    {
        boolean negative = false;
        int maxValue = (1 << length) - 1, minValue = 0;
        if (input.startsWith("-"))
        {
            if (!signed)
                throw new InvalidAssemblyException("Immediate value is signed, expecting unsigned " + length + " bits");
            input = input.substring(1);
            negative = true;
        }
        if (signed)
        {
            maxValue >>= 1;
            minValue = -maxValue - 1;
        }
        int absoluteValue = Integer.parseInt(input, 10);
        if (negative)
        {
            absoluteValue = ~absoluteValue + 1;
        }
        if (absoluteValue < minValue || absoluteValue > maxValue)
        {
            throw new InvalidAssemblyException("Immediate value " + absoluteValue + " out of range for " + length + " bit signed: [" + minValue + ", " + maxValue + "]");
        }
        int maxBits = (1 << length) - 1;
        return absoluteValue & maxBits;
    }

    private Helpers() {}
}
