package core

import chisel3._
import chisel3.util._
import chiseltest._
import chisel3.testers._
import ALU._
import Control._

// class Control_tester extends BasicTester {
//     val dut = Module(new Control)
    
// }

// class ControlTests extends org.scalatest.FlatSpec {
//   "Control" should "pass" in {
//       printf("hex: %d, uhex: %d", 0x3000.S, 0x3000.U)
//     //assert(TesterDriver execute (() => new Control_tester))
//   }
// }