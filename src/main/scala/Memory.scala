package core

import chisel3._
import chisel3.util._

class DMemoryReq extends Bundle {
    val addr = Input(UInt(32.W))
    val data = Input(UInt(32.W))
    val mask = Input(UInt(4.W)) //one hot encoded?
}

class DMemoryResp extends Bundle {
    val data = Output(UInt(32.W))
}

class DMemoryIO extends Bundle {
    val req = Valid(new DMemoryReq)
    val resp = Valid(new DMemoryResp)
}

class DMemory extends Module {
    // https://www.chisel-lang.org/api/latest/chisel3/SyncReadMem.html  read and write is undefined --> this will do as no reads and writes in 1 instruction
    // https://www.chisel-lang.org/api/3.4.3/chisel3/Mem.html           read and write is fine 

    //chapter 14
    val io = IO(new DMemoryIO)

    val mem = SyncReadMem(2048, Vec(4, UInt(8.W)))

    val aligned_addr = (io.req.bits.addr >> 2).asUInt
    val valid_addr = aligned_addr(11,0)
    val data = VecInit(io.req.bits.data(31,24), io.req.bits.data(23,16), io.req.bits.data(15,8),io.req.bits.data(7,0))
    
    val wen = io.req.bits.mask.orR && io.req.valid
    val ren = io.req.valid && !wen

    io.resp.valid := false.B
    io.resp.bits.data := DontCare

    

    //only write when wen is true
    when(wen){
        mem.write(valid_addr, data, io.req.bits.mask.toBools)
    }.elsewhen(ren) {
        io.resp.bits.data := mem.read(valid_addr, ren)
        io.resp.valid := true.B
    }
}