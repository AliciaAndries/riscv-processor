package core

import chisel3._
import chisel3.util._
import org.scalatest._
import chisel3.testers._
import Opcode._
import FPGAInstructions._

class Dataflow_tester extends BasicTester{
    val dut = Module(new Dataflow(true))
    dut.io.io_out_of_bounds := false.B

    val correct_wb = VecInit(
        0.U(32.W),  //firs round is nop
        1.U(32.W),
        225.U(32.W),
        10.U(32.W),
        24.U(32.W),
        235.U(32.W),
        225.U(32.W),
        112.U(32.W),
        241.U(32.W),
        224.U(32.W),
        BigInt(3758096384L).U(32.W),
        241.U(32.W),                   
        BigInt(4043309056L).U(32.W),
        56.U(32.W),                     //pc + 4
        BigInt(4043309056L).U(32.W),
        0.U(32.W),
        120.U(32.W),
        120.U(32.W),
        1.U(32.W),
        225.U(32.W),
        225.U(32.W),
        80.U(32.W),
        225.U(32.W),
        225.U(32.W),
        226.U(32.W),
        1.U(32.W),
        225.U(32.W),
        71.U(32.W),
        0.U(32.W),
        BigInt(14745600L).U(32.W),
        225.U(32.W),
        0.U(32.W),  //load so doesnt matter
        57344.U(32.W),
        0.U(32.W),  //load so doesnt matter
        BigInt(4294959104L).U(32.W),    //already correct cause reads the same as prev ld just interprets it differently
        57344.U(32.W),
        BigInt(3758039040L).U(32.W),
        BigInt(4294967288L).U(32.W),
        BigInt(3758096400L).U(32.W),
        BigInt(3758096384L).U(32.W),
        0.U(32.U),
        BigInt(4294963200L).U(32.W),
        0.U(32.W),  //load so doesnt matter
        224.U(32.W),
        271.U(32.W),
        0.U(32.W),  //load so doesnt matter
        BigInt(4294967266L).U(32.W),
        0.U,
        1.U,
        1.U,
        0.U,
        102400.U(32.W),
        4268.U(32.W),
        180.U(32.W),
        0.U(32.W)
        )
    
    val iMem = Module(new IMemory("/home/alicia/Documents/thesis/riscv-processor/src/test/resources/all.hex"))
    val dMem = Module(new Memory)
    val stages = 5.U

    val (cntr, done) = Counter(true.B, correct_wb.size)
    
    val return_data = 0.U//iMem(dut.io.iMemIO.req.bits.addr>>2.U)
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


    val pc_prev = RegInit(0.U(32.W))
    pc_prev := dut.io.fpgatest.pc
    val wb = dut.io.fpgatest.wb
    val wb_check = correct_wb(cntr)
    val ld = RegInit(true.B)

    when(dut.io.fpgatest.pcsrc === Control.Pl0){
      ld := ~ld
    }

    val prev_is_jump = RegInit(true.B)
    prev_is_jump := dut.io.fpgatest.pcsrc === Control.Jump || dut.io.fpgatest.pcsrc === Control.Br

    val correct_pc = Mux(dut.io.fpgatest.pcsrc === Control.Pl0 && ~ld, dut.io.fpgatest.pc === pc_prev, 
                      Mux(prev_is_jump, true.B,  dut.io.fpgatest.pc === pc_prev + 4.U))

    printf("cntr = %d, correct_wb = %d, wb = %d, correct_pc = %d, pc = %d, inst = %x\n", cntr, wb_check, wb, correct_pc, dut.io.fpgatest.pc >> 2.U, iMem.io.resp.bits.data)
    when(dut.io.fpgatest.pcsrc.orR =/= Control.Pl0 && ~ld){
      assert(wb === wb_check)
    }
    assert(correct_pc)
    when(done) { stop(); stop() } 
}

class DataflowTests extends FlatSpec with Matchers {
  "Dataflow" should "pass" in {
    assert(TesterDriver execute (() => new Dataflow_tester))
  }
} 