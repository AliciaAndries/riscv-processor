package core

import chisel3._
import chisel3.util._


/* ImmGen
    use:
    you need to sign extend to 64 for ld, sd and beq
    ld & sd -> add offset to base register

    for branch also a bit shift to the left then added to PC but why the bit shift?
    Instead of shifting all bits in the instruction-encoded
    immediate left by one in hardware as is conventionally done, the middle bits (imm[10:1]) and sign
    bit stay in fixed positions, while the lowest bit in S format (inst[7]) encodes a high-order bit in B
    format. --> look up reason

    TODO: it impossible that the input for the ALU is greater than 32 so why 64 in architecture book


    sign bit in immediate is always bit 31 of instr

    p16-17 in RISCV manual

*/

object ImmGen {
    val X = 0.U(3.W)
    val I = 1.U(3.W)
    val S = 2.U(3.W)
    val B = 3.U(3.W)
    val U = 4.U(3.W)
    val J = 5.U(3.W)
    val C = 6.U(3.W)       //CSR also need extension -> p55
}

class ImmGenIO extends Bundle{
    val inst = Input(UInt(32.W))
    val immGenCtrl = Input(UInt(3.W))
    val out = Output(UInt(64.W))
}

class ImmGen extends Module {
    val io = IO(new ImmGenIO)

    val b31 =       Mux(io.immGenCtrl === ImmGen.C, 0.U, io.inst(31))
    val b30to20 =   Mux(io.immGenCtrl === ImmGen.U, io.inst(30,20), b31)
    val b19to12 =   Mux(io.immGenCtrl =/= ImmGen.U && io.immGenCtrl =/= ImmGen.J, b31, io.inst(30,20))
    val b11 =       Mux(io.immGenCtrl === ImmGen.B, io.inst(11), 
                        Mux(io.immGenCtrl === ImmGen.U, 0.U,
                        Mux(io.immGenCtrl === ImmGen.J, io.inst(20), b31)))
    val b10to5 =    Mux(io.immGenCtrl === ImmGen.U || io.immGenCtrl === ImmGen.C, 0.U, io.inst(30,25))
    val b4to1 =     Mux(io.immGenCtrl === ImmGen.I || io.immGenCtrl === ImmGen.J, io.inst(24,21),
                        Mux(io.immGenCtrl === ImmGen.U, 0.U, 
                        Mux(io.immGenCtrl === ImmGen.C, io.inst(19,14), io.inst(11,8))))
    val b0 =        Mux(io.immGenCtrl === ImmGen.I, 20.U, 
                        Mux(io.immGenCtrl === ImmGen.S, io.inst(7), 
                        Mux(io.immGenCtrl === ImmGen.C, io.inst(15), 0.U)))

    
    io.out := Cat(b31, b30to20, b19to12, b11, b10to5, b4to1, b0)

    //there is another way to calculate so if slow or a lot of area try it (see for example mini-riscv)
}