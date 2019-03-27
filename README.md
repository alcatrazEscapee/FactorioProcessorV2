# Factorio Processor / Assembly Compiler


This is a repository for a processor I built in Factorio. It is a 3.75 Hz*, 16-Bit RISC Style Processor with 512 Byte ROM, 128 Byte RAM, and a 16-Bit ASCII Character Display. Map download, compiler, example programs and supporting documentation can all be found here.

\* I have gotten it to run at up to 120 Hz with `/c game.speed=32`, which is 1920 UPS. Any more and I'm scared for the health of my PC.

![Splash Image](splash_image.jpg)

---
##### Assembly Compiler

In order to use the compiler, you need to know how to run java via command line:
```
java -jar factoriocompiler.jar [program arguments]
```

The compiler can take a few different arguments, which in no particular order are:

 - `-f` or `--file`: This specifies the input assembly source code. Following this must be a file path.
 - `-l` or `--line`: This specifies a single line of assembly source code as input. Following this must be a valid line of Factorio assembly code. Note this and `-f` are mutually exclusive.
 - `-b` or `--blueprint`: Toggles the blueprint flag, which will turn the resultant assembly into a Factorio ROM Blueprint, ready to be shift-clicked onto the processor. This is by far the easiest and least error-prone way to load programs into the processor.
 - `-d` or `--debug`: Toggles the debug flag, which outputs a bunch more data about the assembly, the bit patterns, the instructions etc.
 
Example Usage:
```
java -jar factoriocompiler.jar -d --file path/to/assembly_code.s --blueprint
```

---
##### Documentation / Processor Specifications

The full processor specifications can be found [here](PROCESSOR.md). This includes documentation on the instruction set (if you want to write example programs), the inner signals of the processor, and also descriptions of my methodology, the design elements including RTN descriptions of the various instructions, and outlines of the components involved.
 
