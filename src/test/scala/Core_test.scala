package core

import chisel3._
import chisel3.util._
import org.scalatest._
import chisel3.testers._

class Core_tester extends BasicTester {
    val dut = Module(new Core(new IMemoryVec))
    
    val (cntr, done) = Counter(true.B, 53)

    printf("counter = %d, wb = %d, pc = %d, halt = %d, pc_ex = %d, reg_input1 = %d,reg_input2 = %d, idex_rd = %d, decode_pc = %d, decodeinst = 0x%x\n", 
    cntr, dut.io.fpgatest.wb, dut.io.fpgatest.pc>>2.U, dut.io.fpgatest.halt, dut.io.fpgatest.pc_ex>>2.U,dut.io.fpgatest.reg_input1, dut.io.fpgatest.reg_input2, dut.io.fpgatest.id_ex_rd, dut.io.fpgatest.decode_pc, dut.io.fpgatest.decode_inst)
    when(done) { stop(); stop() } 
}

class CoreTests extends FlatSpec with Matchers {
  "Core" should "pass" in {
    assert(TesterDriver execute (() => new Core_tester))
  }
}