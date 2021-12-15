package core

import chisel3._
import chisel3.util._
import org.scalatest._
import chisel3.testers._

class Pipelined_Tester extends BasicTester {
    val dut = Module(new Dataflow(true))//Module(new Core(new Data))
    val dMem = Module(new Memory)
    val stages = 5.U
    val (cntr, done) = Counter(true.B, 50)
    val iMem = Module(new IMemory("/home/alicia/Documents/thesis/riscv-processor/src/test/resources/all.hex"))
    /* iMem.io.req.bits.addr := dut.io.iMemIO.req.bits.addr
    iMem.io.req.bits.data := dut.io.iMemIO.req.bits.data
    iMem.io.req.bits.mask := dut.io.iMemIO.req.bits.mask
    iMem.io.req.valid := dut.io.iMemIO.req.valid

    dut.io.iMemIO.resp.bits.data := iMem.io.resp.bits.data
    dut.io.iMemIO.resp.valid := iMem.io.resp.valid */
    dut.io.fpgatest.wmask := 0.U
    dut.io.fpgatest.waddr := 0.U
    dut.io.fpgatest.reg_addr := 0.U
    dut.io.fpgatest.wdata := 0.U
    dut.io.fpgatest.halt_in := false.B
    dut.io.fpgatest.pc_reset := false.B

    val prev_mem = RegInit(0.U(32.W))

    dut.io.io_out_of_bounds := false.B

    iMem.io.req.bits.addr := dut.io.iMemIO.req.bits.addr
    iMem.io.req.bits.mask := dut.io.iMemIO.req.bits.mask
    iMem.io.req.bits.data := dut.io.iMemIO.req.bits.data
    iMem.io.req.valid := dut.io.iMemIO.req.valid
    dut.io.iMemIO.resp.bits.data := iMem.io.resp.bits.data
    dut.io.iMemIO.resp.valid := iMem.io.resp.valid

    dMem.io.req.bits.addr := dut.io.dMemIO.req.bits.addr
    dMem.io.req.bits.data := dut.io.dMemIO.req.bits.data
    dMem.io.req.bits.mask := dut.io.dMemIO.req.bits.mask
    dMem.io.req.valid := dut.io.dMemIO.req.valid

    dut.io.dMemIO.resp.bits.data := dMem.io.resp.bits.data
    dut.io.dMemIO.resp.valid := dMem.io.resp.valid

    printf("cntr = %d, pc = %d, wb = %d, data = %d, mem_addr = %d\n", cntr, dut.io.fpgatest.pc>>2.U, dut.io.fpgatest.wb, dut.io.dMemIO.req.bits.data, dut.io.iMemIO.req.bits.addr>>2.U)

/*     when(dut.io.iMemIO.req.bits.addr>>2.U - 5.U === 13.U){
      assert(prev_mem === 69.U)
      printf("\nload and arithemtic hazards are solved\n")
    } */
    when(dut.io.fpgatest.pc>>2.U === 13.U){
      assert(prev_mem === 69.U)
      printf("\nload and arithemtic hazards are solved\n")
    }
    
    when(dut.io.fpgatest.pc>>2.U === 23.U){
      assert(prev_mem === 6.U)
      printf("\nbeq hazards are solved\n")
    }
    
    when(dut.io.fpgatest.pc>>2.U === 30.U){
      assert(prev_mem === 100.U)
      printf("\njal hazards are solved\n")
    }
    prev_mem := dut.io.dMemIO.req.bits.data
    when(done) { stop(); stop() } 
}

class PipelinedTests extends FlatSpec with Matchers {
  "Pipeline" should "pass" in {
    assert(TesterDriver execute (() => new Pipelined_Tester))
  }
}