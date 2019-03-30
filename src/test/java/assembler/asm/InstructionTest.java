package assembler.asm;

import assembler.util.Helpers;
import assembler.util.InvalidAssemblyException;
import org.jetbrains.annotations.TestOnly;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InstructionTest
{
    @Test
    void testType1() throws Exception
    {
        assertEquals("0000000000000000", parse("call", "FunctionName"));
        assertEquals("0000000000010000", parse("ret", ""));
    }

    @Test
    void testType2() throws Exception
    {
        assertEquals("0100111000000001", parse("add", " r2, r3, r4"));
        assertEquals("0100111000010001", parse("sub", " r2, r3, r4"));
        assertEquals("1111101011100001", parse("and", " r7, r6, r5"));
        assertEquals("0100111001010010", parse("rr", " r2, r3, r4"));
        assertEquals("0000010100100010", parse("ls", " r0, r1, r2"));
        assertEquals("0010010011000001", parse("pow", " ra, ra, ra"));
    }

    @Test
    void testType3() throws Exception
    {
        assertEquals("0100110000000011", parse("stw", " r2, 0(r3)"));
        assertEquals("0110100001000100", parse("ldw", " r3, 4(r2)"));
        assertEquals("0100111111100011", parse("stw", " r2, -2(r3)"));
        assertEquals("0110100000000100", parse("ldw", " r3, SOME_VAR(r2)"));
    }

    @Test
    void testFails()
    {
        // Syntax Errors
        assertThrows(InvalidAssemblyException.class, () -> parse("add", " rx, r1, ra"));
        assertThrows(InvalidAssemblyException.class, () -> parse("add", " r2 r3, r4"));
        assertThrows(InvalidAssemblyException.class, () -> parse("add", " r9, r3, r4"));
        assertThrows(InvalidAssemblyException.class, () -> parse("add", " r-1, r3, r4"));
        assertThrows(InvalidAssemblyException.class, () -> parse("add", " r1, r2, 10"));

        // Incorrect addressing mode
        assertThrows(IndexOutOfBoundsException.class, () -> parse("stw", "r1, r2, 6"));

        // Immediate not in range
        assertThrows(InvalidAssemblyException.class, () -> parse("addi", " r1, r2, 32"));
        assertThrows(InvalidAssemblyException.class, () -> parse("muli", " r1, r2, -33"));
        assertThrows(InvalidAssemblyException.class, () -> parse("ori", " r1, r2, -1"));
        assertThrows(InvalidAssemblyException.class, () -> parse("andi", " r1, r2, 64"));
    }

    @Test
    void testImmediate() throws InvalidAssemblyException
    {
        // Zero
        assertEquals(0, Helpers.parseImmediate("0", 6, true));

        // Max: 31
        assertEquals(31, Helpers.parseImmediate("0x1F", 6, true));
        assertEquals(31, Helpers.parseImmediate("31", 6, true));
        assertEquals(31, Helpers.parseImmediate("037", 6, true));
        assertEquals(31, Helpers.parseImmediate("0b11111", 6, true));
        // Min: -32
        assertEquals(0b100000, Helpers.parseImmediate("0x20", 6, true));
        assertEquals(0b100000, Helpers.parseImmediate("-32", 6, true));
        assertThrows(InvalidAssemblyException.class, () -> Helpers.parseImmediate("63", 6, true));
        assertEquals(0b100000, Helpers.parseImmediate("040", 6, true));
        assertEquals(0b100000, Helpers.parseImmediate("0b100000", 6, true));
        // Out of range
        assertThrows(InvalidAssemblyException.class, () -> Helpers.parseImmediate("32", 6, true));
        assertThrows(InvalidAssemblyException.class, () -> Helpers.parseImmediate("-33", 6, true));
        assertThrows(InvalidAssemblyException.class, () -> Helpers.parseImmediate("0x7F", 6, true));
        assertThrows(InvalidAssemblyException.class, () -> Helpers.parseImmediate("0127", 6, true));
        assertThrows(InvalidAssemblyException.class, () -> Helpers.parseImmediate("0b1111111", 6, true));
    }

    @TestOnly
    private String parse(String key, String input) throws InvalidAssemblyException
    {
        InstructionType type = InstructionType.get(key);
        if (type == null)
        {
            fail("Instruction type is null");
        }
        return new Instruction(type, 0, input, input.split("#")[0].replaceAll(" *", "").split(",")).getEncodedString();
    }
}