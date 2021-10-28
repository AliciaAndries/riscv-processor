package core

import chisel3._
import chisel3.util._
import Instructions._

class CoreIO extends Bundle {
    val same = Output(Bool())
    val pc = Output(Bool())
    val sum = Output(Bool())
    val addi = Output(Bool())

}

class Core extends Module {
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

    io.addi := (dataflow.io.fpgatest.wb === 10.U)
    io.pc := Mux(dataflow.io.fpgatest.pc>>2.U < 32.U, dataflow.io.fpgatest.pc(2), false.B)

/*     //ADD test
    io.sum := (dataflow.io.fpgatest.wb === 20.U)
    io.same := dataflow.io.fpgatest.zero */

 /*   //LDST test
    io.sum := dataflow.io.fpgatest.wb === 10.U
    io.same := true.B */

    val pc_prev = RegInit(dataflow.io.fpgatest.pc)

 //OR
    when(dataflow.io.fpgatest.pc>>2.U === 2.U){
        io.sum := (dataflow.io.fpgatest.wb === 14.U)
        io.same := true.B
    }
    //SUB
    .elsewhen(dataflow.io.fpgatest.pc>>2.U === 5.U){
        io.sum := (dataflow.io.fpgatest.wb === 4.U)
        io.same := true.B
    }
    //AND
    .elsewhen(dataflow.io.fpgatest.pc>>2.U === 8.U){
        io.sum := (dataflow.io.fpgatest.wb === 2.U)
        io.same := true.B
    }
    //ADD
    .elsewhen(dataflow.io.fpgatest.pc>>2.U === 11.U){
        io.sum := (dataflow.io.fpgatest.wb === 20.U)
        io.same := true.B
    }
    //LDST
    .elsewhen(dataflow.io.fpgatest.pc>>2.U === 15.U){
        io.sum := (dataflow.io.fpgatest.wb === 10.U)
        io.same := true.B
    }
    //BEQ NT
    .elsewhen(dataflow.io.fpgatest.pc>>2.U === 17.U){
        io.sum := true.B
        io.same := dataflow.io.fpgatest.zero === false.B
    }.elsewhen(pc_prev>>2.U === 17.U){
        io.sum := true.B
        io.same := pc_prev + 4.U === dataflow.io.fpgatest.pc
    }
    //BEQ T
    .elsewhen(dataflow.io.fpgatest.pc>>2.U === 19.U){
        io.sum := true.B
        io.same := dataflow.io.fpgatest.zero === true.B
    }.elsewhen(pc_prev>>2.U === 19.U){
        io.sum := pc_prev + 16.U === dataflow.io.fpgatest.pc
        io.same := true.B
    }
    .otherwise{
        io.sum := true.B
        io.same := true.B
    }

    pc_prev := dataflow.io.fpgatest.pc
}

object CoreFPGAOut extends App{
    (new chisel3.stage.ChiselStage).emitVerilog(new Core, args)
}