package assembler;

import assembler.asm.Assembly;
import assembler.asm.IInstruction;
import assembler.util.Helpers;
import assembler.util.InvalidAssemblyException;
import org.jetbrains.annotations.TestOnly;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FactorioAssemblerTest
{
    @Test
    void test1()
    {
        // Basic arithmetic instructions
        test("assets/test1.s", 16425, 24633, 35201, 37034, 36907, 43393, 53937, 63937, 65529);
    }

    @Test
    void test2()
    {
        // conditional branching
        test("assets/test2.s", 24665, 18457, 20456, 19449, 20455, 32784);
    }

    @Test
    void testHelloWorld()
    {
        // Prints 'Hello World' to the display
        test("assets/test_hello_world.s", 16473, 18542, 33065, 28676, 26627, 36889, 18457, 28676, 25526, 18489, 36889, 28676, 26627, 36889, 18457, 28676, 25526, 32784, 72, 69, 76, 76, 79, 0, 87, 79, 82, 76, 68, 33, 0);
    }

    @Test
    void testClearDisplay()
    {
        // Clears the output display
        test("assets/test_clear_display.s", 16473, 18542, 41225, 2051, 47097, 18457, 41927, 32784);
    }

    @Test
    void testHelloWorldSubroutine()
    {
        // Prints 'Hello World' to the display, using a modular subroutine asm style
        test("assets/test_hello_world_subroutine.s", 57421, 64622, 65533, 16397, 18542, 18973, 24585, 192, 16397, 18542, 19069, 24713, 32, 32784, 65497, 23555, 31763, 39971, 32861, 36974, 36877, 28161, 34820, 35843, 18457, 27673, 34820, 33718, 23556, 31764, 39972, 64569, 16, 72, 69, 76, 76, 79, 0, 87, 79, 82, 76, 68, 0);
    }

    @TestOnly
    void test(String fileName, int... expectedBytes)
    {
        String data = Helpers.loadResource(fileName);
        try
        {
            Assembly asm = FactorioAssembler.build("test", data);
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