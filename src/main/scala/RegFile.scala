package core

import chisel3._
import chisel3.util._

/* 
    you need to be able to access RegFile 2 times in 1 clock-cycle when pipelined cause of the writeback and decoding part && could be same addr
    would it be benificial for Dmemory? No because there is only max 1 Dmem access in 1 clockcycle
    but important if you use Mem do you read the old or new version?
*/

class RegFileIO extends Bundle {
    val raddr1 = Input(UInt(5.W)) //addr is 5 wide
    val raddr2 = Input(UInt(5.W))
    val waddr = Input(UInt(5.W))
    val wdata = Input(UInt(32.W))
    val wen = Input(Bool())
    val rs1 = Output(UInt(32.W))
    val rs2 = Output(UInt(32.W))
}

class RegFile extends Module {
    val io = IO(new RegFileIO)

    def risingedge(x: Bool) = x && !RegNext(x)
    def fallingedge(x: Bool) = !x && RegNext(x)

    val revClk = Wire(new Clock)
    revClk := (~clock.asUInt()(0)).asBool.asClock()

    val out1 = RegInit(0.U)
    val out2 = RegInit(0.U)

    val wire1 = WireDefault(0.U)

    //val reg = Mem(32, UInt(32.W))  //32 registers -> only 5 bit addr so cant have any bigger
    val reg = RegInit(VecInit(Seq.fill(32)(0.U(32.W))))
    
    withClock(clock){
        printf("rising\n")
        out1 := Mux(io.raddr1.orR, reg(io.raddr1), 0.U)
        out2 := Mux(io.raddr2.orR, reg(io.raddr2), 0.U)
    }

    when(io.wen && io.waddr.orR){
        withClock(clock){
            printf("falling\n")
            //printf("written: %d\n", io.wdata)
            reg(io.waddr) := io.wdata
        }
    }

    io.rs1 := Mux(io.raddr1.orR, reg(io.raddr1), 0.U)
    io.rs2 := Mux(io.raddr2.orR, reg(io.raddr2), 0.U)

    /* when(io.waddr === io.raddr1){
        io.rs1 := io.wdata
    } */

    //printf("read: %d, %d\n", out1, out2)

}
