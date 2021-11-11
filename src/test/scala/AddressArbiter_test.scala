package core

import chisel3._
import chisel3.util._
import org.scalatest._
import chisel3.testers._
import scala.math.BigInt

class AddressArbiter_Tester extends BasicTester {
    val dut = Module(new AddressArbiter)
    val uart = Module(new Uart(128,16))
    
    val addrs = VecInit(10.U, 1024.U, 1032.U, 1028.U, 2000.U)
    val data = VecInit(20.U, 1.U, 2.U, 3.U)

    val (cntr, done) = Counter(true.B, addrs.size+1)

    dut.io.io_req.bits.addr := addrs(cntr)
    dut.io.io_req.bits.mask := "b1111".U
    dut.io.io_req.bits.data := data(cntr)
    dut.io.io_req.valid := true.B
    dut.io.uart_port <> uart.io.dataPort
    uart.io.serialPort.rx := false.B


    printf("cntr = %d, clockDivisor = %d, txQueue.valid = %d, xQueue.bits = %d, uart_out = %d, led_io = %d, dmem_valid = %d,, dmem_data = %d, out_of_bounds = %d\n",
            cntr, dut.io.uart_port.clockDivisor, dut.io.uart_port.txQueue.valid, dut.io.uart_port.txQueue.bits, dut.io.uart_out, dut.io.led_io, dut.io.mem_req.valid, dut.io.mem_req.bits.data, dut.io.address_not_in_use)

    when(done) { stop(); stop() } 
}

class AddressArbiterTests extends FlatSpec with Matchers {
  "AddressArbiter" should "pass" in {
    assert(TesterDriver execute (() => new AddressArbiter_Tester))
  }
}