The main branch has all the different versions of the design which are:

-Non-pipelined
-Pipelined
-Pipelined ALU split        (overall fastest)
-Pipelined extra register   (highest clock speed)
-Pipelined combined         (lowest area)

### Dependencies

#### JDK 8 or newer

Install java 8 or newer

#### SBT

https://www.scala-sbt.org/download.html  

### Verilator

https://verilator.org/guide/latest/install.html
(apt-get version is too old so install it from git)

### Tests
(normally all tests can be run with "sbt test" but at the moment it gives errors that don't happen when all tests are run seperately)

Test the "extender" (12 to 32 bits)
sbt "testOnly core.ImmGenTests"

Test the ALU
sbt "testOnly core.ALUTests"  

Test the Memory
sbt "testOnly core.MemoryTests" 

Test the Control
sbt "testOnly core.ControlTests" 

Test the Registerfile
sbt "testOnly core.RegFileTests" 

Test the dataflow
sbt "testOnly core.DataflowTests" 
(sometimes there is an assert fail that two reg_idx's are the same, this is necause they are randomly generated in the test code and to be sure that all edge cases are being hit they are going to keep being completely randomly generated for a while longer)

### Generate Verilog
make
    will generate verilog with initialised instruction memory. The instructions are those currently in all_uart.s

sbt run
    - CoreFPGAOutHardCodedInsts has hardcoded instructions sourced from "InstructionsFpgaTests.scala". 
        If everything is working correctly io.sum and io.same should always output true
    - core.CoreFPGAOutInitMem has BRAM memory that is initialised with a file, the file (test.mem) should be included in the sources of the project.
        This however isn't working yet as Vivado simply optimizes the core away when using this method.

-> the generated Core.v contains the whole project