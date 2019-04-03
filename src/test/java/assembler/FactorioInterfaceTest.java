package assembler;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FactorioInterfaceTest
{
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUpStreams()
    {
        outContent.reset();
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void restoreStreams()
    {
        System.setOut(originalOut);
    }

    @Test
    void cliNoArgs()
    {
        FactorioAssembler.main();
        assertEquals("Expecting arguments, found none.\r\n", outContent.toString());
    }

    @Test
    void cliNoFile()
    {
        FactorioAssembler.main("-f");
        assertEquals("Expected another argument after '-f'\r\n", outContent.toString());

    }

    @Test
    void cliNoFile2()
    {
        FactorioAssembler.main("--file");
        assertEquals("Expected another argument after '--file'\r\n", outContent.toString());
    }

    @Test
    void cliNoLine()
    {
        FactorioAssembler.main("-l");
        assertEquals("Expected another argument after '-l'\r\n", outContent.toString());
    }

    @Test
    void cliNoLine2()
    {
        FactorioAssembler.main("--line");
        assertEquals("Expected another argument after '--line'\r\n", outContent.toString());
    }

    @Test
    void noInputData()
    {
        FactorioAssembler.main("-d");
        assertEquals("Require either -f or -l for input data.\r\n", outContent.toString());
    }

    @Test
    void multipleLine()
    {
        FactorioAssembler.main("-l", "test", "--line", "other");
        assertEquals("Can't specify multiple data input sources\r\n", outContent.toString());
    }

    @Test
    void multipleData()
    {
        FactorioAssembler.main("-l", "test", "-f", "src/test/resources/test1.s");
        assertEquals("Can't specify multiple data input sources\r\n", outContent.toString());
    }

    @Test
    void multipleFile()
    {
        FactorioAssembler.main("--file", "src/test/resources/test1.s", "-f", "src/test/resources/assets/test1.s");
        assertEquals("Can't specify multiple data input sources\r\n", outContent.toString());
    }
}
