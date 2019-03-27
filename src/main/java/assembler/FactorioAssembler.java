/*
 * Part of Factorio Assembler
 * Copyright (c) 2019 - 2019 Alex O'Neill
 * See the project LICENCE.md for more information
 */

package assembler;

import java.util.List;

import assembler.asm.Assembly;
import assembler.asm.InstructionTemplate;
import assembler.asm.InstructionType;
import assembler.blueprint.Blueprints;
import assembler.util.Helpers;
import assembler.util.InvalidAssemblyException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Main class and CLI for the compiler / assembler
 *
 * @author Alex O'Neill
 */
public final class FactorioAssembler
{
    public static void main(String... args)
    {
        if (args.length == 0)
        {
            System.out.println("Expecting arguments, found none.");
            return;
        }

        String input = null, name = null;
        boolean blueprint = false, debug = false, hasData = false;
        for (int i = 0; i < args.length; i++)
        {
            switch (args[i].toLowerCase())
            {
                case "-f":
                case "--file":
                    if (args.length <= i + 1)
                    {
                        System.out.println("Expected another argument after '" + args[i] + "'");
                        return;
                    }
                    if (hasData)
                    {
                        System.out.println("Can't specify multiple data input sources");
                        return;
                    }
                    input = Helpers.loadFile(args[i + 1]);
                    if (input == null)
                    {
                        System.out.println("Can't find file '" + args[i + 1] + "'");
                        return;
                    }
                    name = args[i + 1].split("\\.")[0].replaceAll("[^a-zA-Z0-9_-]", "");
                    i++;
                    hasData = true;
                    break;
                case "-l":
                case "--line":
                    if (args.length <= i + 1)
                    {
                        System.out.println("Expected another argument after '" + args[i] + "'");
                        return;
                    }
                    if (hasData)
                    {
                        System.out.println("Can't specify multiple data input sources");
                        return;
                    }
                    input = args[i + 1];
                    i++;
                    hasData = true;
                    name = input.replaceAll("[^a-zA-Z0-9_-]", "");
                    break;
                case "-b":
                case "--blueprint":
                    blueprint = true;
                    break;
                case "-d":
                case "--debug":
                    debug = true;
                    break;
                default:
                    System.out.println("Unrecognized argument '" + args[i] + "'");
            }
        }

        if (input == null || !hasData)
        {
            System.out.println("Require either -f or -l for input data.");
            return;
        }

        System.out.printf("Compiling with arguments: [%s]\n", String.join(" ", args));
        Assembly asm;
        try
        {
            asm = build(name, input);
        }
        catch (InvalidAssemblyException e)
        {
            System.out.println("Error compiling the assembly: ");
            e.printStackTrace();
            e.printData();
            return;
        }
        catch (Exception e)
        {
            System.out.println("Unknown Exception occurred. This is a bug: ");
            e.printStackTrace();
            return;
        }

        if (debug)
        {
            System.out.println(asm);
        }

        if (blueprint)
        {
            String blueprintString = Blueprints.encode(asm);
            System.out.println("Blueprint String:");
            System.out.println(blueprintString);
        }
    }

    @NotNull
    public static Assembly build(@NotNull String name, @NotNull String input) throws InvalidAssemblyException
    {
        Assembly assembly = new Assembly(name);
        List<String> inputLines = Helpers.getLinesUnformatted(input);

        while (inputLines.size() > 0)
        {
            String line = inputLines.remove(0);
            List<String> results = compileLine(assembly, line);
            if (results != null)
            {
                inputLines.addAll(0, results);
            }
        }

        assembly.applyLinker();
        return assembly;
    }

    @Nullable
    private static List<String> compileLine(Assembly assembly, String line) throws InvalidAssemblyException
    {
        try
        {
            StringBuilder inputBuilder = new StringBuilder(line);
            StringBuilder keywordBuilder = new StringBuilder();

            while (inputBuilder.length() > 0)
            {
                // Pop a character into the keyword buffer
                Helpers.nextChar(keywordBuilder, inputBuilder);
                String keyword = keywordBuilder.toString();
                String[] args = inputBuilder.toString().replaceAll(" ", "").split(",");

                if ("exit".equals(keyword))
                {
                    assembly.addExit();
                    return null;
                }

                if (inputBuilder.length() > 0)
                {
                    if (InstructionType.keys().contains(keyword) && inputBuilder.charAt(0) == ' ')
                    {
                        // basic instruction
                        assembly.addInstruction(keyword, keyword + inputBuilder, args);
                        return null;
                    }
                    else if (InstructionTemplate.keys().contains(keyword) && inputBuilder.charAt(0) == ' ')
                    {
                        // template instruction
                        return InstructionTemplate.get(keyword).convert(args);
                    }
                    else if (".asciz".equals(keyword) && inputBuilder.charAt(0) == ' ')
                    {
                        inputBuilder.deleteCharAt(0);
                        assembly.addData(inputBuilder.toString().toUpperCase());
                        return null;
                    }
                    else if (inputBuilder.charAt(0) == ':')
                    {
                        // labels
                        assembly.addLabel(keyword);
                        inputBuilder.deleteCharAt(0);
                        keywordBuilder = new StringBuilder();
                    }
                }
            }

            if (keywordBuilder.length() > 0)
            {
                System.out.println("Unable to parse: " + keywordBuilder);
            }
            return null;
        }
        catch (InvalidAssemblyException e)
        {
            e.attachData("Current Line: " + line, assembly);
            throw e;
        }
        catch (Exception e)
        {
            InvalidAssemblyException wrapper = new InvalidAssemblyException("Unknown exception occurred during parsing", e);
            wrapper.attachData("Current Line: " + line, assembly);
            throw wrapper;
        }
    }
}
