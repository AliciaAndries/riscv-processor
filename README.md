this branch has all instrcutions and is pipelined (5 stages)

- branch destinations are calculated in execute stage -> 2 nops when taken
    -> could do in decode stage but then mayb between inst before and beq a nop
- arith data hazards need 4 possible inputs for each alu operand because "write" to RegFile takes place on rising edge after write command
    -> could be less hardware if write was on falling edge and read on rising
- wrong mem addresses for now corrected, no fit thrown


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
sbt run
    - CoreFPGAOutHardCodedInsts has hardcoded instructions sourced from "InstructionsFpgaTests.scala". 
        If everything is working correctly io.sum and io.same should always output true
    - core.CoreFPGAOutInitMem has BRAM memory that is initialised with a file, the file (test.mem) should be included in the sources of the project.
        This however isn't working yet as Vivado simply optimizes the core away when using this method.

-> the generated Core.v contains the whole project