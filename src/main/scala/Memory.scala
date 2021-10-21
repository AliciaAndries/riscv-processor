package core

import chisel3._
import chisel3.util._

class MemoryReq extends Bundle {
    val addr = UInt(32.W)
    val data = UInt(32.W)
    val mask = UInt(4.W) //one hot encoded?
}

class MemoryResp extends Bundle {
    val data = UInt(32.W)
}

class MemoryIO extends Bundle {
    val req = Flipped(Valid(new MemoryReq))
    val resp = Valid(new MemoryResp)
}

class Memory/* (pipelined: Boolean = false) */ extends Module {
    //syncreadmem handy because it only reads and writes on the next clockcycle so is good for pipelined version as you dont need an extra register to hold rdata
    //for non pipelined annoying because you need to wait a clockcycle

    //chapter 14
    val io = IO(new MemoryIO)

    val aligned_addr = (io.req.bits.addr >> 2.U).asUInt
    val valid_addr = aligned_addr(7,0)

    val data = VecInit(io.req.bits.data(7,0), io.req.bits.data(15,8), io.req.bits.data(23,16), io.req.bits.data(31,24))
    val wen = io.req.bits.mask.orR && io.req.valid
    val ren = io.req.valid && !wen
    io.resp.valid := false.B
    io.resp.bits.data := DontCare

/*     if(pipelined){ */
        val mem = SyncReadMem(256, Vec(4, UInt(8.W)))

        //only write when wen is true
        when(wen){
            mem.write(valid_addr, data, io.req.bits.mask.asBools)
        }.elsewhen(ren) {
            val data = mem.read(valid_addr,ren)
            //io.resp.bits.data := Cat(data(0), data(1), data(2), data(3))    //.reverse wasnt working?
            io.resp.bits.data := Cat(data(3), data(2), data(1), data(0))
            io.resp.valid := true.B
        }
/*     } else {
        val mem = Mem(8, Vec(4, UInt(8.W)))

        when(wen){
            mem(valid_addr) = Muxdata //io.req.bits.mask.asBools
        }.elsewhen(ren) {
            val data = mem(valid_addr)
            //io.resp.bits.data := Cat(data(0), data(1), data(2), data(3))    //.reverse wasnt working?
            io.resp.bits.data := Cat(data(3), data(2), data(1), data(0))
            io.resp.valid := true.B
        } 
    }*/
}