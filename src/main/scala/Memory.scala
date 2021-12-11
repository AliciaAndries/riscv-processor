package core

import chisel3._
import chisel3.util._
import chisel3.util.experimental.loadMemoryFromFileInline
//import FPGAInstructions._

object MemorySize {
    val BMemBytes = 500
    val IMemBytes = 1000
}

class MemoryReq extends Bundle {
    val addr = UInt(32.W)
    val data = UInt(32.W)
    val mask = UInt(4.W)
}

class MemoryResp extends Bundle {
    val data = UInt(32.W)
}

class MemoryIO extends Bundle {
    val req = Flipped(Valid(new MemoryReq))
    val resp = Valid(new MemoryResp)
}

class Memory(Memsize: Int = 500) extends Module {
    
    //syncreadmem handy because it only reads and writes on the next clockcycle so is good for pipelined version as you dont need an extra register to hold rdata
    //for non pipelined annoying because you need to wait a clockcycle

    //chapter 14
    val io = IO(new MemoryIO)

    val aligned_addr = (io.req.bits.addr >> 2.U).asUInt
    val valid_addr = aligned_addr

    val data = VecInit(io.req.bits.data(7,0), io.req.bits.data(15,8), io.req.bits.data(23,16), io.req.bits.data(31,24))
    val wen = io.req.bits.mask.orR && io.req.valid
    val ren = io.req.valid && !wen
    io.resp.valid := false.B
    io.resp.bits.data := DontCare

    val mem = SyncReadMem(Memsize, Vec(4, UInt(8.W)))

    //only write when wen is true
    when(wen){
        mem.write(valid_addr, data, io.req.bits.mask.asBools)
    }.elsewhen(ren) {
        val data = mem.read(valid_addr,ren)
        //io.resp.bits.data := Cat(data(0), data(1), data(2), data(3))    //.reverse wasnt working?
        io.resp.bits.data := Cat(data(3), data(2), data(1), data(0))
        io.resp.valid := true.B
    }
}

trait IMem{
    val io = new MemoryIO
}

class IMemory(dir: String, Memsize: Int = 500) extends Module with IMem {
    override val io = IO(new MemoryIO)
    val nop = "b00000000000000000000000000010011".U(32.W)

    val aligned_addr = (io.req.bits.addr >> 2.U).asUInt
    val valid_addr = Mux(aligned_addr < MemorySize.IMemBytes.U(32.W), true.B, false.B)
    
    val wen = io.req.bits.mask.orR && io.req.valid
    val ren = io.req.valid && !wen
    io.resp.valid := false.B
    io.resp.bits.data := DontCare

    val mem = SyncReadMem(Memsize, UInt(32.W))
    
    
    loadMemoryFromFileInline(mem, dir)
    

    //only write when wen is true
    when(wen){
        mem.write(aligned_addr, io.req.bits.data)
    }.elsewhen(ren) {
        val data = mem.read(aligned_addr,ren)
        io.resp.bits.data := Mux(valid_addr, data, nop)
        io.resp.valid := true.B
    }
}

class IMemoryVec extends Module with IMem {
    override val io = IO(new MemoryIO)

    val aligned_addr = (io.req.bits.addr >> 2.U).asUInt
    val valid_addr = aligned_addr(7,0)

    //val data = Cat(io.req.bits.data(7,0), io.req.bits.data(15,8), io.req.bits.data(23,16), io.req.bits.data(31,24))
    val wen = io.req.bits.mask.orR && io.req.valid
    val ren = io.req.valid && !wen
    io.resp.valid := false.B
    io.resp.bits.data := DontCare

    val mem = RegInit(VecInit(0.U))

    //only write when wen is true
    when(wen){
        mem(valid_addr) := io.req.bits.data
    }.elsewhen(ren) {
        val data = mem(valid_addr)
        //io.resp.bits.data := Cat(data(0), data(1), data(2), data(3))    //.reverse wasnt working?
        io.resp.bits.data := data
        io.resp.valid := true.B
    }
}

object IMemorydriver extends App{
    (new chisel3.stage.ChiselStage).emitVerilog(new IMemory("load_mem_test"), args)
}

object DMemorydriver extends App{
    (new chisel3.stage.ChiselStage).emitVerilog(new Memory(500), args)
}