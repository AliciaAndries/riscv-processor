package core

import chisel3._
import chisel3.util._
import org.scalatest._
import chisel3.testers._
import Opcode._
import FPGAInstructions._

class Dataflow_tester extends BasicTester{
    val dut = Module(new Dataflow(true))

    val correct_wb = VecInit(
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
        52.U(32.W),                     //pc + 4
        0.U(32.W),                      //two nops 
        0.U(32.W),
        BigInt(4043309056L).U(32.W),
        0.U(32.W),                    
        0.U(32.W),
        0.U(32.W),
        120.U(32.W),
        120.U(32.W),
        0.U(32.W),
        0.U(32.W),
        1.U(32.W),
        0.U(32.W),
        0.U(32.W),
        225.U(32.W),
        225.U(32.W),
        76.U(32.W),
        0.U(32.W),
        0.U(32.W),
        225.U(32.W),
        0.U(32.W),
        0.U(32.W),
        225.U(32.W),
        226.U(32.W),
        1.U(32.W),
        0.U(32.W),
        0.U(32.W),
        225.U(32.W),
        0.U(32.W),
        0.U(32.W),
        71.U(32.W),
        BigInt(14745600L).U(32.W),
        225.U(32.W),
        57344.U(32.W),
        BigInt(4294959104L).U(32.W),    //already correct cause reads the same as prev ld just interprets it differently
        0.U(32.W),
        57344.U(32.W),
        BigInt(3758039040L).U(32.W),
        BigInt(4294967288L).U(32.W),
        BigInt(3758096400L).U(32.W),
        BigInt(3758096384L).U(32.W),
        0.U(32.U),
        BigInt(4294963200L).U(32.W),
        224.U(32.W),
        271.U(32.W),
        BigInt(4294967266L).U(32.W),
        0.U,
        0.U,
        1.U,
        1.U,
        0.U,
        102400.U(32.W),
        4264.U(32.W),
        176.U(32.W),
        0.U(32.W)
        )
    
    val iMem = RegInit(all)
    val dMem = Module(new Memory)
    val stages = 5.U

    val (cntr, done) = Counter(true.B, iMem.size+10)
    
    val return_data = iMem(dut.io.iMemIO.req.bits.addr>>2.U)
    dut.io.iMemIO.resp.bits.data := return_data
    dut.io.iMemIO.resp.valid := true.B

    dMem.io.req.bits.addr := dut.io.dMemIO.req.bits.addr
    dMem.io.req.bits.data := dut.io.dMemIO.req.bits.data
    dMem.io.req.bits.mask := dut.io.dMemIO.req.bits.mask
    dMem.io.req.valid := dut.io.dMemIO.req.valid

    dut.io.dMemIO.resp.bits.data := dMem.io.resp.bits.data
    dut.io.dMemIO.resp.valid := dMem.io.resp.valid

    val pc_prev = RegInit(0.U)
    pc_prev := dut.io.fpgatest.pc
    val wb = dut.io.fpgatest.wb
    val wb_check = Mux(cntr < 5.U, 0.U, correct_wb(cntr-5.U))
    val correct_pc = MuxLookup(cntr, dut.io.fpgatest.pc === pc_prev + 4.U, Seq(
                12.U -> (dut.io.fpgatest.pc === pc_prev - 8.U),
                13.U -> (dut.io.fpgatest.pc === pc_prev + 12.U),
                16.U -> (dut.io.fpgatest.pc === pc_prev - 8.U),
                17.U -> (dut.io.fpgatest.pc === pc_prev + 12.U),
                20.U -> (dut.io.fpgatest.pc === pc_prev - 8.U),
                21.U -> (dut.io.fpgatest.pc === pc_prev + 12.U),
                24.U -> (dut.io.fpgatest.pc === pc_prev - 8.U),
                25.U -> (dut.io.fpgatest.pc === pc_prev + 12.U),
                27.U -> (dut.io.fpgatest.pc === pc_prev),
                30.U -> (dut.io.fpgatest.pc === pc_prev),
                32.U -> (dut.io.fpgatest.pc === pc_prev),
                41.U -> (dut.io.fpgatest.pc === pc_prev),
                44.U -> (dut.io.fpgatest.pc === pc_prev),
                52.U -> (dut.io.fpgatest.pc === 4264.U + 16.U)
                ))
    printf("cntr = %d, correct_wb = %d, wb = %d, correct_pc = %d, pc = %d\n", cntr, wb_check, wb, correct_pc, dut.io.fpgatest.pc >> 2.U)
    //assert(wb === wb_check)
    when(done) { stop(); stop() } 
}

class DataflowTests extends FlatSpec with Matchers {
  "Dataflow" should "pass" in {
    assert(TesterDriver execute (() => new Dataflow_tester))
  }
} 