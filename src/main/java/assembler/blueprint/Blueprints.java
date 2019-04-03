/*
 * Part of Factorio Assembler
 * Copyright (c) 2019 - 2019 Alex O'Neill
 * See the project LICENCE.md for more information
 */

package assembler.blueprint;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import assembler.asm.Assembly;
import assembler.asm.IInstruction;
import assembler.util.Helpers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Blueprints
{
    private static final String BLUEPRINT_128W, BLUEPRINT_256W;

    static
    {
        BLUEPRINT_128W = Helpers.loadResource("128w_rom_blueprint.json").replaceAll("[\n\r\t ]+", "");
        BLUEPRINT_256W = Helpers.loadResource("256w_rom_blueprint.json").replaceAll("[\n\r\t ]+", "");
    }

    @Nullable
    @SuppressWarnings("unused")
    public static String decode(@NotNull String input)
    {
        byte[] bytes = Base64.getDecoder().decode(input.substring(1));
        byte[] result, buffer = new byte[1024];

        Inflater inflater = new Inflater();
        inflater.setInput(bytes, 0, bytes.length);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(bytes.length))
        {
            while (!inflater.finished())
            {
                int count = inflater.inflate(buffer);
                outputStream.write(buffer, 0, count);
            }
            result = outputStream.toByteArray();
        }
        catch (DataFormatException | IOException e)
        {
            return null;
        }
        String outputString = new String(result, 0, result.length, StandardCharsets.UTF_8);

        System.out.println(outputString);
        return outputString;
    }

    @Nullable
    public static String encode(@NotNull Assembly asm)
    {
        int size = asm.size() >= 128 ? 256 : 128;
        String input = asm.size() >= 128 ? BLUEPRINT_256W : BLUEPRINT_128W;
        for (int i = 0; i < size; i++)
        {
            IInstruction inst = asm.getInstruction(i);
            String replacement = inst == null ? "0" : "" + inst.getEncoded();
            input = input.replaceAll("\"MEMORY_VALUE_" + i + "\"", replacement);
        }
        input = input.replaceAll("PROGRAM_NAME", asm.getName());

        byte[] result, buffer = new byte[1024];
        Deflater deflater = new Deflater();
        deflater.finish();
        deflater.setInput(input.getBytes(StandardCharsets.UTF_8));
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream())
        {
            while (!deflater.finished())
            {
                int count = deflater.deflate(buffer);
                outputStream.write(buffer, 0, count);
            }
            result = outputStream.toByteArray();
        }
        catch (IOException e)
        {
            return null;
        }
        return "0" + Base64.getEncoder().encodeToString(result);
    }
}
