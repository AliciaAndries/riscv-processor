package core

import chisel3._
import chisel3.util._
import org.scalatest._
import chisel3.testers._
import FPGAInstructions._

class Pipelined_Tester extends BasicTester {
    val dut = Module(new Dataflow)//Module(new Core(new Data))
    val iMem = RegInit(all)
    val dMem = Module(new Memory)
    val stages = 5.U
    val (cntr, done) = Counter(true.B, iMem.size)

    /* iMem.io.req.bits.addr := dut.io.iMemIO.req.bits.addr
    iMem.io.req.bits.data := dut.io.iMemIO.req.bits.data
    iMem.io.req.bits.mask := dut.io.iMemIO.req.bits.mask
    iMem.io.req.valid := dut.io.iMemIO.req.valid

    dut.io.iMemIO.resp.bits.data := iMem.io.resp.bits.data
    dut.io.iMemIO.resp.valid := iMem.io.resp.valid */
    val return_data = iMem(dut.io.iMemIO.req.bits.addr>>2.U)
    dut.io.iMemIO.resp.bits.data := return_data
    dut.io.iMemIO.resp.valid := true.B

    dMem.io.req.bits.addr := dut.io.dMemIO.req.bits.addr
    dMem.io.req.bits.data := dut.io.dMemIO.req.bits.data
    dMem.io.req.bits.mask := dut.io.dMemIO.req.bits.mask
    dMem.io.req.valid := dut.io.dMemIO.req.valid

    dut.io.dMemIO.resp.bits.data := dMem.io.resp.bits.data
    dut.io.dMemIO.resp.valid := dMem.io.resp.valid

    printf("cntr = %d, pc = %d, wb = %d\n", cntr, dut.io.fpgatest.pc, dut.io.fpgatest.wb)

    when(cntr === iMem.size.U - 1.U -1.U){  //second to last pipeline stage so -1
      assert(dut.io.dMemIO.req.bits.data === 69.U)
      printf("load and arithemtic hazards are solved")
    }
    
    when(done) { stop(); stop() } 
}

class PipelinedTests extends FlatSpec with Matchers {
  "Pipeline" should "pass" in {
    assert(TesterDriver execute (() => new Pipelined_Tester))
  }
}