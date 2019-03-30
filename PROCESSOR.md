# Processor Specifications

### Contents

1. [Basic Specifications](#1-basic-specifications)
   - 1.1 Guidelines
   - 1.2 Register File
   - 1.3 ALU
   - 1.4 Memory
   - 1.5 Instruction Set
   - 1.6 Operation Instructions
2. [Instruction Specifications](#2-instruction-specifications)
   - 2.1 Instruction Table
   - 2.2 Assembler Pseudo-Instructions
   - 2.3 Instruction Types
3. [Processor Specifications](#3-processor-specifications)
   - 3.1 Clock and Step Outline
   - 3.2 Other Structures
4. [Implementation](#4-implementation)
   - 4.1 IR Decoding Signals
   - 4.2 Main Bus Signals
   - 4.3 Internal Memory Signals
   - 4.4 Control Signal Generator
5. [Peripherals](#5-peripherals)
   - 5.1 16-Character ASCII Display
   - 5.2 ASCII Keyboard and Input Buffer

---
### 1. Basic Specifications

##### 1.1 Guidelines
 - 16 Bit Word Length (Decimal Encoded / DE Signals)
 - 5-Step RISC Style Architecture + Instruction Set
 - Initial Memory: 256 Byte ROM, 256 Byte RAM
 - Instruction Set to focus on ALU operations (with combinator logic) over immediate values
 - Support for Memory-based peripherals such as I/O, Factory Control, or Display
 - 3.75 Hz Clock / 1.3 s per instruction

##### 1.2 Register File
 - General Purpose Registers r0 - r7
 - 3-Bit Addressable
 - Dual Ported, Single Memory Design
 - r0 = Zero Register
 - r1 = ra = Link Register
 - r7 = sp = Stack Pointer
 - r2-r6 = General Purpose Registers

##### 1.3 ALU
 - Support for all native factorio combinator operations, including operations not typical on a RISC style architecture (modulo, power, etc.)
 - Additional "compound" operations (rotate, xnor, etc.)
 - Operations done on 32-Bit signals, which have been sign extended from 16 bits

##### 1.4 Memory
 - 512 Byte / 256 Word ROM (Read Only Memory)
 - Implemented as constant combinators - used for program storage
 - Word-Addressable - locations 0 - 255
 - 128 Byte / 64 Word RAM (Random Access Memory)
 - Implemented as chained 8x16-Bit SR-Latch Units
 - Word-Addressable - locations 256 - 320
 - Assembler Macros: `LAST_RAM_WORD` = 320

##### 1.5 Instruction Set
 - Basic 16-Bit RISC Style Instruction Set.
 - Standard 4-Bit opcode
 - 3-Bit Register Identifier fields RD, RS, RT
 - Immediate values:
 - R-Type: `[3b - RD][3b - RS][3b - RT][7b - OP]`
 - Most ALU Instructions (important use of the extended OP)
 - C-Type: `[3b - RD][3b - RS][6b - IMM][4b - OP]`
 - All branch instructions
 - Immediate value instructions (both signed and logical 6b immediate)
 - Memory operations
 - L-Type: `[11b - IMM][5b - OP]`
 - Call / Return Instructions
 - Special `exit` instruction (asynchronous)
 
##### 1.6 Operation Instructions
 - The Main controller (clock generator / SR-running latch) has a few lights to represent the current state of the processor. In order from left to right (each 2 lights = 1 signal) these are:

Exit Flag:
 - If **RED**, The processor has reached an exit instruction, causing the clock to halt.
 - If **GREEN**, The processor is ready.

Startup Flag:
 - If **YELLOW**, The user has left the "Startup" Constant Combinator in the "on" position. This can cause a conflict with any exit instructions. To start the processor, it is only necessary to switch on the combinator for a brief period of time.
 - If **GREEN**, The processor is ready.

Reset Flag:
 - If **RED**, The processor is shut down / in reset mode. No instructions can be executed while this is active. This will wipe all internal registers (but it won't wipe memory). The processor should be reset to clear the exit flag, when trying to restart a program.
 - If **GREEN**, The processor is ready.
 
Running Flag:
 - If **RED**, The processor's clock is stopped. This is likely due to a manual stop, or termination from an exit instruction.
 - If **GREEN**, the processor's clock is running. If the running flag and automatic flag are set, the processor is currently executing instructions
 
Automatic Flag:
 - If **RED**, The processors clock is disengaged from the control network, meaning no instructions are being executed. Used for debugging purposes only, toggled by a constant combinator.
 - If **GREEN**, The processor is in normal execution mode.

Standard Flag Configurations:

1. GREEN - GREEN - GREEN - RED - GREEN:
   - Processor Ready to begin execution
   - (user turns on the startup flag)
2. GREEN - YELLOW - GREEN - GREEN - GREEN:
   - Processor is active (with startup flag left on)
   - (user turns off the startup flag)
3. GREEN - GREEN - GREEN - GREEN - GREEN:
   - Processor is active (this is what it should be)
   - (program halts)
4. RED - GREEN - GREEN - RED - GREEN:
   - The program has finished execution
   - (user turns on reset flag)
5. GREEN - GREEN - RED - RED - GREEN:
   - The processor is in shutdown / rebooting
   - (user turns off the reset flag, should lead to state 1.)

Extra Info:
 - There are also three white lamps "pointing" to a combinator in the register file. That combinator will be outputting the current values of r1 - r7 at all times. (useful for debugging purposes)

---
### 2. Instruction Specifications

2.1 Instruction Table

 - Note that most of these instructions are very similar if not identical in assembly usage to the Nios-II DE0 instruction set, which I used as a template and inspiration for this instruction set. (Although the implementation of the instruction set and the overall processor is quite different.)
 - All immediate values (denoted with IMM, 6b-IMM, or 12b-IMM) represent immediate values. The exception is the `movia` pseudoinstruction, which can take a label or a 12 bit signed immediate value (range \[-1024, 1023])
 - Arithmetic instructions (such as add, subtract, power, etc.) will sign-extend their arguments, so the range of the 6-bit immediate is \[-32, 31]. Logical instructions (such as and, or, xor, etc.) will zero-extend their arguments, so the range of the 6-bit immediate is \[0, 63]

Opcode      | Name                 | Assembly  | Fields        | ID    | Type
------------|----------------------|-----------|---------------|-------|-------
0 0000      | Call                 | call      | IMM           | 1     | 1
1 0000      | Return               | ret       | n/a           | 2     | 1
000 0001    | Add                  | add       | rX, rY, rZ    | 3     | 2
001 0001    | Subtract             | sub       | rX, rY, rZ    | 4     | 2
010 0001    | Multiply             | mul       | rX, rY, rZ    | 5     | 2
011 0001    | Divide               | div       | rX, rY, rZ    | 6     | 2
100 0001    | Power                | pow       | rX, rY, rZ    | 7     | 2
101 0001    | Modulo               | mod       | rX, rY, rZ    | 8     | 2
110 0001    | Bitwise AND          | and       | rX, rY, rZ    | 9     | 2
111 0001    | Bitwise OR           | or        | rX, rY, rZ    | 10    | 2
000 0010    | Bitwise XOR          | xor       | rX, rY, rZ    | 11    | 2
001 0010    | Bitwise XNOR         | xnor      | rX, rY, rZ    | 12    | 2
010 0010    | Left Shift           | ls        | rX, rY, rZ    | 13    | 2
011 0010    | Right Shift          | rs        | rX, rY, rZ    | 14    | 2
100 0010    | Left Rotate          | lr        | rX, rY, rZ    | 15    | 2
101 0010    | Right Rotate         | rr        | rX, rY, rZ    | 16    | 2
110 0010    |                      |           |               | 17    |
111 0010    |                      |           |               | 18    |
0011        | Memory Store         | stw       | rX, IMM(rY)   | 19    | 3
0100        | Memory Load          | ldw       | rX, IMM(rY)   | 20    | 3
0101        | Break If Equal       | beq       | rX, rY, LABEL | 21    | 4
0110        | Break If Not Equal   | bne       | rX, rY, LABEL | 22    | 4
0111        | Break If Greater     | bgt       | rX, rY, LABEL | 23    | 4
1000        | Break If Less        | blt       | rX, rY, LABEL | 24    | 4
1001        | Add Immediate        | addi      | rX, rY, IMM   | 25    | 5
1010        | Multiply Immediate   | muli      | rX, rY, IMM   | 26    | 5
1011        | Divide Immediate     | divi      | rX, rY, IMM   | 27    | 5
1100        | AND Immediate        | andi      | rX, rY, IMM   | 28    | 6
1101        | OR Immediate         | ori       | rX, rY, IMM   | 29    | 6
1110        | Left Shift Immediate | lsi       | rX, rY, IMM   | 30    | 6
1111        | Right Shift Immediate| rsi       | rX, rY, IMM   | 31    | 6


2.2: Assembler / Compiler Pseudo-Instructions:

Name                       | Assembly | Fields        | Actual Implementation
---------------------------|----------|---------------|-------------------------
Move                       | mov      | rX, rY        | add rX, r0, rY
Move Immediate             | movi     | rX, 6b-IMM    | addi rX, r0, IMM
Move Immediate Unsigned    | moviu    | rX, 6b-IMM    | ori rX, r0, IMM
Move Address               | movia    | rX, 12b-IMM   | ori rX, r0, IMM\[11-6] / lshifti rX, rX, 6 / ori rX, rX, IMM\[5-0]
Subtract Immediate         | subi     | rX, rY, 6b-IMM| addi rX, rY, -IMM
Break If Greater or Equal  | bge      | rX, rY, LABEL | blt rY, rX, LABEL
Break If Less or Equal     | ble      | rX, rY, LABEL | bgt rY, rX, LABEL
Break If Zero              | brz      | rX, LABEL     | beq rX, r0, LABEL
Break If Not Zero          | bnz      | rX, LABEL     | bne rX, r0, LABEL
Break Unconditional        | br       | LABEL         | beq r0, r0, LABEL
No-op                      | nop      |               | add r0, r0, r0

\* The `exit` instruction shares an opcode with `return`. It is given by the encoding 1000000000010000. This instruction will stop the processor's clock and terminate program execution. 

Additionally, the compiler will recognize directives that start with a `.`:

 - `.asciz [some raw text]`: This will insert a null-terminated string into the program ROM data. See the provided hello world program for example usage.
 - `.malloc [LABEL] [amount]`: Tracks a memory amount, and assigns the label to the beginning of the memory in RAM.


##### 2.3 Instruction Types

1. Type 1:
   - High-Level Program Control: call / return
   - Format: `[11b (sign extend) - IMM][5b - OP]`
   - Both instructions have specific RTN which modifies the PC during cycle 4

2. Type 2:
   - register ALU operations
   - Format: `[3b - RD][3b - RS][3b - RT][7b - OP]`
   - three register inputs, extended opcode, with all ALU operations used
   - execution is very similar, aside from a different ALU control signal asserted

3. Type 3:
   - Memory access: store / load
   - Format: `[3b - RD][3b - RS][6b (logical extend) - IMM][4b - OP]`
   - 6-Bit Logical extended offset (word addressed)
   - All steps are utilized
  
4. Type 4:
   - Low-Level Program Control: breaks
   - Format: `[3b - RD][3b - RS][6b (sign extend) - IMM][4b - OP]`
   - 6b sign extended offset - modifies PC in step 4
   - compare operation done by ALU - then PC modified based on ALU Control out signals
  
5. Type 5 / 6:
   - Register Immediate operations
   - Both use a immediate value (5 = sign extend, 6 = logical extend)
   - Format: `[3b - RD][3b - RS][6b - IMM][4b - OP]`
   - Similar to register ALU counterpart operations


##### 2.4 Instruction RTN
 - All Instructions have a common first step:
 - STEP 1: `PC <- [PC] + 1`, Memory Read (Address `[PC]`), `[IR] <- Memory Data`
 - Some control signals are implicit, i.e. `RA <- [r0]` since that requires a RF-A-ID of 0, or default
 - These are important (they must be asserted) but require no specific logic to implement
 - Registers that don't have load enable (i.e. RY, RZ, RM) are still denoted in RTN if they experience change

`call IMM`:
 - STEP 2: noop
 - STEP 3: noop
 - STEP 4: `PC <- [PC] + 12b-Imm-Sign`, `RY <- [PC]`
 - STEP 5: `r1 <- [RY]`

`return`:
 - STEP 2: `RA <- [r1]`
 - STEP 3: ALU Add, `RZ <- ALU Result`
 - STEP 4: `PC <= [RZ]`
 - STEP 5: noop

TYPE 2:

`op rD, rS, rT`
 - STEP 2: `RA <- [rS]`, `RB <- [rT]`
 - STEP 3: ALU op, `RZ <- ALU Result`
 - STEP 4: `RY <- [RZ]`
 - STEP 5: `rD <- [RY]`

TYPE 3:

`store rD, IMM(rS)`
 - STEP 2: `RA <- [rS]`, `RB <- [rD]`, `ALU-B-IN <- 6b-Imm-Sign`
 - STEP 3: ALU Add, `RZ <- ALU Result`, `RM <- [RB]`
 - STEP 4: Memory Write (Data `[RM]`, Address `[RZ]`)
 - STEP 5: noop

`load rD, IMM(rS)`
 - STEP 2: `RA <- [rS]`, `ALU-B-IN <- 6b-Imm-Sign`
 - STEP 3: ALU Add, `RZ <- ALU Result`
 - STEP 4: Memory Read (Address `[RZ]`), `RY <- Memory Data`
 - STEP 5: `rD <- [RY]`

TYPE 4:

`brcmp rD, rS, IMM`
 - STEP 2: `RA <- [rD]`, `RB <- [rS]`
 - STEP 3: ALU cmp, `RZ <- ALU Result`
 - STEP 4: If `[RZ] = 1`: `PC <- [PC] + 6-Imm-Sign`
 - STEP 5: noop

TYPE 5 / 6:

`opi rD, rS, IMM`
 - STEP 2: `RA <- [rS]`, `ALU-B-In <- 6b-Imm-(Sign / Logical if type 5 / 6)`
 - STEP 3: ALU op, `RZ <- ALU Result`
 - STEP 4: `RY <- [RZ]`
 - STEP 5: `rD <- [RY]`

---
### 3. Processor Specifications

##### 3.1 Clock and Step Outline
 - 5 Distinct Steps
 - Step Counter as a single 16-Bit D-FF with CLR
 - Steps labeled as: 1, 2, 3, 4, 5
 - STEP 1: Instruction Fetch, IR Load, PC Load
 - STEP 2: IR Decode, RA, RB Load
 - STEP 3: ALU Operation, RZ Load
 - STEP 4: Memory Fetch, RY Load or PC Load
 - STEP 5: RF Load
 - Clock is done with a counter from 0 to 16
 - Updates happen every game tick, so speed is 3.75 Hz (at 60 UPS)
 - My laptop (32 MB / i7-8850U / 2.6 GHz) can run with 32x speed (120 Hz, or 0.041s / instruction)

##### 3.2 Internal Structure
 - RA, RB, RY, RZ implemented as 16-Bit D-FF with CLR
 - IR, PC, 16-Bit D-FF with CLR, LE
 - Other Units: RF, ALU, PC-IAG, RB-Mux, RY-Mux, PMI
 - ROM: Constant Combinator based ROM (4x blocks with 4 rows of 32-Bytes each)
 - RAM: SR-Latch blocks (4x blocks of 16-Bytes each)
 - Input: Full ASCII Keyboard + Input Buffer D-FF
 - Output: 16-Character Display with 1x 32-Byte RAM block

---
### 4. Implementation

##### 4.1 IR Decoding Signals
 - Instructions are encoded via one of the following patterns, MBS to LSB Left to Right:
 - R-Type: `[3b - RD][3b - RS][3b - RT][7b - OP]`
 - C-Type: `[3b - RD][3b - RS][6b - IMM][4b - OP]`
 - L-Type: `[11b - IMM][5b - OP]`

Input:
`I` - IR Value

Output:
 - `I` - Instruction ID
 - `T` - Instruction Type
 - `X` - Register RD
 - `Y` - Register RS
 - `Z` - Register RT
 - `1` - 11-Bit Imm, Sign Extend
 - `5` - 6-Bit Imm, Sign Extend
 - `6` - 6-Bit Imm, Logical Extend


##### 4.2 Main Bus Signals

**DATA (RED)**
 - `A` - RA Output
 - `B` - RB Output
 - `M` - RM Output
 - `Z` - RZ Output
 - `Y` - RY Output
 - `P` - PC Output
 - `1` - 12-Bit Imm, Sign Extend
 - `5` - 6-Bit Imm, Sign Extend
 - `6` - 6-Bit Imm, Logical Extend
 - `N` - Memory Return Data

**CONTROL (GREEN)**
 - `K` - Global CLK (Clock) Signal
 - `R` - Global Active-Low Async. Reset Signal
 - `V` - Global Enable Signal (for clock and step counter)
 - `X` - Global Exit Program Signal (asserted when an EXIT is reached)
 - `A` - RF Address for RA
 - `B` - RF Address for RB
 - `C` - RF Address for RY (Write)
 - `F` - RF Write Signal
 - `I` - IR Load Enable
 - `P` - PC Load Enable
 - `E` - RB-Mux Select (1: RF, 2: 6-Imm-Sign, 3: 6-Imm-Logical)
 - `G` - RY-Mux Select (1: RZ, 2: PC, 3: Memory Data)
 - `H` - PC-IAG Select (1: PC + 1, 2: PC + 6-Imm-Sign, 3: PC + 12-Imm-Sign, 4: RZ)
 - `M` - Memory Read
 - `W` - Memory Write
 - `J` - ALU Operation Select (16+ choices)
 - `L` - Memory Address Select (1: PC, 2: RZ)
 - `S` - Step Counter Value


##### 4.3 Internal Memory Signals

**DATA (RED)**
 - `S` - Memory Address
 - `I` - Input Data
 - `O` - Output Data

**CONTROL (GREEN)**
 - `R` - Read Signal
 - `W` - Write Signal

Internal SRAM Signals:
**INPUT (RED)**
 - `S` - Set
 - `R` - Reset
 - `M` - Memory Address

**OUTPUT (GREEN)**
 - `O` - Memory Value


##### 4.4 Control Signal Generator
 - Two control signals are asserted via external circuitry: `K` (CLK) and `R` (CLR)
 - Other control signals are asserted via combinational logic from input values
 - Input values consist of: IR output fields (`ITXYZ156`), step counter (`S`), and choice other data values (RZ)
 - Logic is laid out in the following pattern: `(S=0 AND (p or..)) OR (S=1 AND (q or..))` ..  
 - Signals that only activate on certain steps only utilize certain columns in the above derivation.
 - Signals that are constant per instruction also ignore the above pattern and just present a series of OR statements

3-Bit Address `A`: (RF Address for RA)
 - (return) I=2: 1
 - (op) T=2: `Y`
 - (load/store) T=3: `Y`
 - (brcmp) T=4: `X`
 - (opi sign) T=5: `Y`
 - (opi logic) T=6: `Y`

3-Bit Address `B`: (RF Address for RB)
 - (op) T=2: `Z`
 - (store) I=19: `X`
 - (brcmp) T=4: `Y`

3-Bit Address `C`: (RF Address for RY (Write))
 - (call) I=1: 1
 - (op) T=2: `X`
 - (load) I=20: `X`
 - (opi sign) T=5: `X`
 - (opi logic) T=6: `X`

Control `F`: (RF Write Signal)
 - `S=5 AND (T=1, T=2, I=20, T=5, T=6)`

Control `I`: (IR Load Enable)
 - S=1

Control `P`: (PC Load Enable)
 - S=1
 - S=4 AND (T=1, T=4: RZ) *This is a special case if T=4, output the RZ value*

Control `E`: (RB-Mux Select, 1: RB, 2: 6-Imm-Sign, 3: 6-Imm-Logical)
 - T=2: 1
 - T=3: 2
 - T=4: 1
 - T=5: 2
 - T=6: 3

Control `G`: (RY-Mux Select, 1: RZ, 2: PC, 3: Memory Data)
 - I=1: 2
 - T=2: 1
 - I=20: 3
 - T=5: 1
 - T=6: 1

Control `H`: (PC-IAG Select, 1: PC + 1, 2: PC + 6-Imm-Sign, 3: PC + 12-Imm-Sign, 4: RZ)
 - S=1: 1
 - S=4 AND I=1: 3
 - S=4 AND I=2: 4
 - S=4 AND T=4: 2

Control `M`: (Memory Read)
 - S=1: 1
 - S=4 AND I=20: 1

Control `W`: (Memory Write)
 - S=4 AND I=19: 1
 
Control `L`: (Memory Address Select, 1: PC, 2: RZ)
 - S=1: 1
 - S=4: 2

Control `J`: (ALU Operation Select, 16+ choices)
 - I=2: Add
 - T=2: Op (given by `I`)
 - T=3: Add
 - T=4: Compare (given by `I`)
 - T=5: Op (given by `I`)
 - T=6: Op (given by `I`)


---
### 5. Peripherals

##### 5.1 16-Character ASCII Display
 - Each character uses a 25x25 pixel area, encoded with a 25-bit binary number.
 - The font used is [Visitor](https://www.dafont.com/visitor.font), with some tweaks.
 - Currently supported characters are 0-9, A-Z, and some punctuation. Relevant ASCII codes are 33 - 90 (inclusive)
 - Note only uppercase ASCII character codes are supported. The assembly compiler will automatically convert all input text to uppercase.
 - The encoded display pattern is then expanded into 25 signals A through Y, which are used as the control inputs for each lamp
 - The base memory location for the display is 320 (in the assembly, `CHAR_OUT_LOC`), and it occupies the next 16 spots, one for each character location
 - The memory is constructed from a slightly modified 32-Byte RAM block - very similar to the four blocks used in processor RAM.
 - Writing to this location with a valid ASCII character code will 'print' that character to the screen.


##### 5.2 ASCII Keyboard and Input Buffer
 - The keyboard is a QWERTY Keyboard layout, with 25x25 pixel displays for each character. Characters are triggered by the activation of a constant combinator under the key display, which causes the key in question to turn green.
 - The keyboard has a shift key, which works as intended, including adjusting the display to the alternate keys
 - All supported ASCII codes by the display are able to be inputted via the keyboard.
 - The keyboard includes an enter key, useful for program control when letting the user input strings.
 - The keyboard can store one character to the input buffer at a time. Each key press counts as a single character. All keys must be fully released before the buffer can accept a new key press.
 - The key buffer occupies memory slot 336 (in the assembly, `CHAR_IN_LOC`). Reading this location will get the current key held, and also clear the buffer. If there is no key, it will return zero.


The keyboard and buffer operate based on a synchronous Finite State Machine, which is linked to the processor clock signal. The FSM has four states, which are stored in the left D-FF, and an additional D-FF which stores the current key.

FSM Specifications:

 - State 0: Reset
     - This state is the default state when `R` is low
     - It unconditionally leads to state 1
 - State 1: Wait for Key Press
     - If `I > 0`: (key data is asserted): goto state 2
     - If `I = 0`: stay in state 1
 - State 2: Store Key
     - If `I = 0`: (key press was too quick): goto state 1
     - Else, goto state 3
     - Output: Key buffer Load Enable is asserted, and the `D` input is set to `I`
 - State 3: Wait + Read Key
     - If `E = 1` (read asserted by processor): goto state 4
     - If `E = 0`: stay in state 3
     - Output: Key buffer Load Enable is asserted, and the `D` input is set to zero.
     - Output if `E = 1`: `O` is set to the output of the key buffer
 - State 4: Wait for Key Release
     - If `I = 0`: goto state 1
     - If `I > 0`: stay in state 4
     

Internal Key Buffer Signals:
 - `K`: Global Clock Signal
 - `R`: Global Reset Signal
 - `Q`: Current FSM State
 - `E`: Read Signal
 - `I`: Key Data Input (from keyboard)
 - `O`: Key Data Output (sent to processor)