package core

import chisel3._
import chisel3.util._
import Instructions._
import FPGAInstructions._

class CoreIO extends Bundle {
    val same = Output(Bool())
    val pc = Output(Bool())
    val sum = Output(Bool())
    val addi = Output(Bool())

}

class Core extends Module {

    val correct_wb = VecInit(
        1.U(32.W),
        225.U(32.W),
        10.U(32.W),
        235.U(32.W),
        225.U(32.W),
        117.U(32.W),
        235.U(32.W),
        224.U(32.W),
        BigInt(3758096384L).U(32.W),
        225.U(32.W),                   
        BigInt(3774873600L).U(32.W),
        52.U(32.W),                     //pc + 4
        BigInt(3774873600L).U(32.W),
        0.U(32.W),                    
        0.U(32.W),
        64.U(32.W),
        1.U(32.W),
        225.U(32.W),
        225.U(32.W),
        76.U(32.W),
        225.U(32.W),
        71.U(32.W),
        0.U(32.W),
        BigInt(14745600L).U(32.W),
        255.U(32.W),
        0.U(32.W),
        10.U(32.W),
        102400.U(32.W),
        4200.U(32.W),
        112.U(32.W),
        0.U(32.W)
        )

    val io = IO(new CoreIO)

    val dataflow = Module(new Dataflow)
    val dMem = Module(new Memory)
    val iMem = Module(new IMemoryVec)

    iMem.io.req.bits.addr := dataflow.io.iMemIO.req.bits.addr
    iMem.io.req.bits.data := dataflow.io.iMemIO.req.bits.data
    iMem.io.req.bits.mask := dataflow.io.iMemIO.req.bits.mask
    iMem.io.req.valid := dataflow.io.iMemIO.req.valid

    dataflow.io.iMemIO.resp.bits.data := iMem.io.resp.bits.data
    dataflow.io.iMemIO.resp.valid := iMem.io.resp.valid

    dMem.io.req.bits.addr := dataflow.io.dMemIO.req.bits.addr
    dMem.io.req.bits.data := dataflow.io.dMemIO.req.bits.data
    dMem.io.req.bits.mask := dataflow.io.dMemIO.req.bits.mask
    dMem.io.req.valid := dataflow.io.dMemIO.req.valid

    dataflow.io.dMemIO.resp.bits.data := dMem.io.resp.bits.data
    dataflow.io.dMemIO.resp.valid := dMem.io.resp.valid

    val (cntr, done) = Counter(true.B, 31)

    io.addi := (dataflow.io.fpgatest.wb === 10.U)
    io.pc := Mux(dataflow.io.fpgatest.pc>>2.U < 32.U, dataflow.io.fpgatest.pc(2), false.B)
    val pc_prev = RegInit(0.U)

    val wb = dataflow.io.fpgatest.wb
    io.sum := (correct_wb(cntr) === wb)
    io.same := MuxLookup(cntr, dataflow.io.fpgatest.pc === pc_prev + 4.U, Seq(
                12.U -> (dataflow.io.fpgatest.pc === pc_prev - 8.U),
                13.U -> (dataflow.io.fpgatest.pc === pc_prev + 12.U),
                16.U -> (dataflow.io.fpgatest.pc === pc_prev - 8.U),
                17.U -> (dataflow.io.fpgatest.pc === pc_prev + 12.U),
                20.U -> (dataflow.io.fpgatest.pc === pc_prev - 8.U),
                21.U -> (dataflow.io.fpgatest.pc === pc_prev +12.U),
                30.U -> (dataflow.io.fpgatest.pc === 4200.U + 16.U)
                ))

    pc_prev := dataflow.io.fpgatest.pc
}

object CoreFPGAOut extends App{
    (new chisel3.stage.ChiselStage).emitVerilog(new Core, args)
}