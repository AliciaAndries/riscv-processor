### Dependencies

#### JDK 8 or newer

Install java 8 or newer

#### SBT

https://www.scala-sbt.org/download.html  

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
(Core CoreFPGAOut generates both memory and dataflow)