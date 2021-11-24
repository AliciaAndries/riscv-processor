package core

import chisel3._
import chisel3.util._
import chisel3.experimental.BaseModule
import Instructions._
import FPGAInstructions._

class CoreIO extends Bundle {
    //val fpgatest = new FpgaTestIO
    val ledio = Output(UInt(1.W))
    val uartSerialPort = new UARTSerialPort()
}

class Core[T <: BaseModule with IMem](imemory: => T, test: Boolean) extends Module {

    val io = IO(new CoreIO)

    val dataflow = Module(new Dataflow)
    val dMem = Module(new Memory)
    val iMem = Module(imemory)

    val fifoLength  = 128
    val rxOverclock = 16
    val uart = Module(new Uart(fifoLength, rxOverclock))

    val addressArbiter = Module(new AddressArbiter)

    val read_value = WireDefault(0.U(32.W))
    val read_valid = WireDefault(false.B)

    iMem.io.req.bits.addr := dataflow.io.iMemIO.req.bits.addr
    iMem.io.req.bits.data := dataflow.io.iMemIO.req.bits.data
    iMem.io.req.bits.mask := dataflow.io.iMemIO.req.bits.mask
    iMem.io.req.valid := dataflow.io.iMemIO.req.valid

    dataflow.io.iMemIO.resp.bits.data := iMem.io.resp.bits.data
    dataflow.io.iMemIO.resp.valid := iMem.io.resp.valid


    addressArbiter.io.io_req <> dataflow.io.dMemIO.req

    ////////////////////////////////////// Memory read/write //////////////////////////////////////
    dMem.io.req <> addressArbiter.io.mem_req
    read_value := dMem.io.resp.bits.data
    read_valid := dMem.io.resp.valid

    //////////////////////////////////////        UART       //////////////////////////////////////
    uart.io.dataPort <> addressArbiter.io.uart_port
    io.uartSerialPort <> uart.io.serialPort
    when(addressArbiter.io.uart_out.orR){
        read_value := addressArbiter.io.uart_out
        read_valid := true.B
    }
    //////////////////////////////////////   LED operation   //////////////////////////////////////
    io.ledio := addressArbiter.io.led_io

    //io.fpgatest := dataflow.io.fpgatest
    dataflow.io.dMemIO.resp.bits.data := read_value
    dataflow.io.dMemIO.resp.valid := read_valid

    dataflow.io.io_out_of_bounds := addressArbiter.io.address_not_in_use
}

object CoreFPGAOutHardCodedInsts extends App{
    (new chisel3.stage.ChiselStage).emitVerilog(new Core(new IMemoryVec, false), args)
}

object CoreFPGAOutInitMem extends App{
    (new chisel3.stage.ChiselStage).emitVerilog(new Core(new IMemory("Core_tester"), true), args)
}