package core

import chisel3._
import chisel3.util._
import chisel3.experimental.BaseModule
import Instructions._
//import FPGAInstructions._

class CoreTestIO extends CoreIO {
    val fpgatest = new FpgaTestIO
}

class CoreIO extends Bundle {
    val ledio = Output(UInt(1.W))
    val uartSerialPort = new UARTSerialPort()
}

class CoreTest[T <: BaseModule with IMem, P <: BaseModule with DataflowTrait](DMemSize:  Int = 500, imemory: => T, test: Boolean,  Dataflow: => P = 
new Dataflow(true)) extends Module {

    val io = IO(new CoreTestIO)

    val dMem = Module(new Memory(DMemSize))
    val b = Module(new BaseCore(DMemSize, imemory, Dataflow))
    ////////////////////////////////////// Memory read/write //////////////////////////////////////
    b.io.testio <> io.fpgatest
    io.ledio := b.io.ledio
    io.uartSerialPort <> b.io.uartSerialPort
    dMem.io.resp <> b.io.dMem.resp
    when(io.fpgatest.halt_in){
        dMem.io.req.valid := true.B
        dMem.io.req.bits.addr := io.fpgatest.waddr
        dMem.io.req.bits.data := io.fpgatest.wdata
        dMem.io.req.bits.mask := io.fpgatest.wmask
    } .otherwise{
        dMem.io.req <> b.io.dMem.req
    }
}

class Core[T <: BaseModule with IMem, P <: BaseModule with DataflowTrait](DMemSize:  Int = 500, imemory: => T, test: Boolean, Dataflow: => P = Module(new Dataflow(true))) extends Module {

    val io = IO(new CoreIO)

    val b = Module(new BaseCore(DMemSize, imemory, Dataflow))
    val dMem = Module(new Memory(DMemSize))
    //val dMem = Module(new MemoryTest("/home/alicia/Documents/thesis/riscv-processor/src/test/official_resources/rv32ui-p-sh.hex"))

    b.io.testio <> DontCare
    io.ledio := b.io.ledio
    io.uartSerialPort <> b.io.uartSerialPort
    ////////////////////////////////////// Memory read/write //////////////////////////////////////
    dMem.io <> b.io.dMem
}

class BaseCoreIO extends CoreIO{
    val dMem = Flipped(new MemoryIO)
    val testio = new FpgaTestIO
}

class BaseCore[T <: BaseModule with IMem, P <: BaseModule with DataflowTrait](DMemSize: Int, imemory: => T, Dataflow: => P) extends Module{
    val io = IO(new BaseCoreIO)

    val dataflow = Module(Dataflow)
    //val dMem = Module(new Memory(DMemSize))
    //val dMem = Module(new MemoryTest("/home/alicia/Documents/thesis/riscv-processor/src/test/official_resources/rv32ui-p-sh.hex"))
    val iMem = Module(imemory)

    val fifoLength  = 128
    val rxOverclock = 16
    val uart = Module(new Uart(fifoLength, rxOverclock))

    val addressArbiter = Module(new AddressArbiter(DMemSize))

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
    io.dMem.req <> addressArbiter.io.mem_req
    read_value := io.dMem.resp.bits.data
    read_valid := io.dMem.resp.valid

    //////////////////////////////////////        UART       //////////////////////////////////////
    uart.io.dataPort <> addressArbiter.io.uart_port
    io.uartSerialPort <> uart.io.serialPort
    when(addressArbiter.io.uart_out.orR){
        read_value := addressArbiter.io.uart_out
        read_valid := true.B
    }
    //////////////////////////////////////   LED operation   //////////////////////////////////////
    io.ledio := addressArbiter.io.led_io

    dataflow.io.fpgatest <> io.testio
    dataflow.io.dMemIO.resp.bits.data := read_value
    dataflow.io.dMemIO.resp.valid := read_valid

    dataflow.io.io_out_of_bounds := addressArbiter.io.address_not_in_use
}
/* object CoreFPGAOutHardCodedInsts extends App{
    (new chisel3.stage.ChiselStage).emitVerilog(new CoreTest(new IMemoryVec, false), args)
} */

object CorePipelined extends App{
    (new chisel3.stage.ChiselStage).emitVerilog(new Core(500,new IMemory("/home/alicia/Documents/thesis/riscv-processor/src/test/resources/all_uart.hex"), false, new Dataflow(false)), args)
}

object CoreNonPipelined extends App{
    (new chisel3.stage.ChiselStage).emitVerilog(new Core(500,new IMemory("/home/alicia/Documents/thesis/riscv-processor/src/test/resources/all_uart.hex", 1000), false, new DataflowNotPipelined(false)), args)
}

object CorePipelineALUSplit extends App{
    (new chisel3.stage.ChiselStage).emitVerilog(new Core(500,new IMemory("/home/alicia/Documents/thesis/riscv-processor/src/test/resources/all_uart.hex", 1000), false, new DataflowALUSplit(false)), args)
}