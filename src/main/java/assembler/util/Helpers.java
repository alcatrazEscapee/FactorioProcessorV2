/*
 * Part of Factorio Assembler
 * Copyright (c) 2019 - 2019 Alex O'Neill
 * See the project LICENCE.md for more information
 */

package assembler.util;

import java.io.*;
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
            throw new Error("Resource '" + fileName + "' not found.");
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input)))
        {
            return reader.lines().reduce((x, y) -> x + "\n" + y).orElse("");
        }
        catch (IOException e)
        {
            throw new Error("Resource '" + fileName + "' not found.");
        }
    }

    public static boolean saveFile(String fileName, String fileData)
    {
        Path filePath = Paths.get(fileName);
        try (BufferedWriter writer = Files.newBufferedWriter(filePath))
        {
            writer.write(fileData);
        }
        catch (IOException e)
        {
            return false;
        }
        return true;
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

    private Helpers() {}
}
