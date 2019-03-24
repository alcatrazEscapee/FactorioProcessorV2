package assembler;

import assembler.code.Assembly;
import assembler.code.IInstruction;
import assembler.util.Helpers;
import assembler.util.InvalidAssemblyException;
import org.jetbrains.annotations.TestOnly;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FactorioAssemblerTest
{
    @Test
    void build1()
    {
        // Basic arithmetic instructions
        test("assets/test1.s", 16425, 24633, 35201, 37034, 36907, 43393, 53937, 63937, 65529);
    }

    @Test
    void build2()
    {
        // conditional branching
        test("assets/test2.s", 24665, 18457, 20456, 19449, 20455, 32784);
    }

    @Test
    void testHelloWorld()
    {
        test("assets/test_hello_world.s", 16473, 18542, 33065, 28676, 26627, 36889, 18457, 28676, 25526, 18489, 36889, 28676, 26627, 36889, 18457, 28676, 25526, 32784, 72, 69, 76, 76, 79, 0, 87, 79, 82, 76, 68, 33, 0);
    }

    @Test
    void testClearDisplay()
    {
        test("assets/test_clear_display.s", 16473, 18542, 41225, 2051, 47097, 18457, 41927, 32784);
    }

    @TestOnly
    void test(String fileName, int... expectedBytes)
    {
        String data = Helpers.loadResource(fileName);
        try
        {
            Assembly asm = FactorioAssembler.INSTANCE.build("test", data);
            assertEquals(expectedBytes.length, asm.size());
            assertArrayEquals(expectedBytes, asm.getInstructions().stream().mapToInt(IInstruction::getEncoded).toArray());

            assertEquals("test", asm.getName());
        }
        catch (InvalidAssemblyException e)
        {
            fail(e);
        }
    }
}