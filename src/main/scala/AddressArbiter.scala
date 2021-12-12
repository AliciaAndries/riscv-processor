package core

import chisel3._
import chisel3.util._
import MemorySize._

class AddressArbiterIO extends Bundle {
    val io_req = Flipped(Valid(new MemoryReq()))
    val mem_req = Valid(new MemoryReq) 
    val led_io = Output(Bool())
    val uart_out = Output(UInt(8.W))
    val uart_port = Flipped(new UARTPort())
    val address_not_in_use = Output(Bool())
}

class AddressArbiter(DataMemSize: Int = MemorySize.BMemBytes) extends Module {
    val io = IO(new AddressArbiterIO)
    
    io.address_not_in_use := false.B

    io.mem_req.valid := false.B
    io.mem_req.bits.addr := 0.U
    io.mem_req.bits.mask := 0.U
    io.mem_req.bits.data := 0.U

    val clockDivisor = RegInit(0.U(8.W))
    val uart_out = RegInit(0.U(8.W))
    io.uart_port.clockDivisor := clockDivisor
    io.uart_port.txQueue.valid := false.B
    io.uart_port.txQueue.bits  := 0.U
    io.uart_port.rxQueue.ready := false.B
    io.uart_out := 0.U

    io.led_io := false.B

    when (io.io_req.valid){
        ////////////////////////////////////// Memory read/write //////////////////////////////////////
        when(io.io_req.bits.addr < (DataMemSize * 4).U){
            io.io_req <> io.mem_req
        } 
        //////////////////////////////////////   LED operation   //////////////////////////////////////
        .elsewhen(io.io_req.bits.addr === (DataMemSize * 4).U){
            io.led_io := io.io_req.bits.data(0)
        }
        //////////////////////////////////////        UART       //////////////////////////////////////
        .elsewhen(io.io_req.bits.addr < (DataMemSize * 4 + 20).U){
            // Reads
            when(io.io_req.valid && !io.io_req.bits.mask.orR) {
                when(io.io_req.bits.addr(4, 0) === 0x04.U) {
                    /* RX */
                    when(io.uart_port.rxQueue.valid) {
                    io.uart_port.rxQueue.ready  := true.B
                    uart_out                    := io.uart_port.rxQueue.bits
                    }
                }
                    /* Status */
                    .elsewhen(io.io_req.bits.addr(4, 0) === 0x08.U) {
                    uart_out := Cat(io.uart_port.txFull, io.uart_port.rxFull, io.uart_port.txEmpty, io.uart_port.rxEmpty)
                    }
                    /* Clock divisor */
                    .elsewhen(io.io_req.bits.addr(4, 0) === 0x10.U) {
                    uart_out := clockDivisor
                    }
                    /* Invalid */
                    .otherwise(uart_out := 0.U)
                }
            // Writes (TX)
            when(io.io_req.valid && io.io_req.bits.mask.orR) {
                when(io.io_req.bits.addr(4, 0) === 0x04.U) {
                    /* TX */
                    io.uart_port.txQueue.valid := true.B
                    io.uart_port.txQueue.bits  := io.io_req.bits.data(7, 0)
                }
                /* clock divisor */
                .elsewhen(io.io_req.bits.addr(4,0) === 0x08.U) {
                clockDivisor := io.io_req.bits.data(7, 0)
                }
            }
        } .otherwise {
            io.address_not_in_use := true.B
        }
        io.uart_out := uart_out
    }
}