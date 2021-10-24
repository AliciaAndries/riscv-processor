module ImmGen(
  input  [31:0] io_inst,
  input  [2:0]  io_immGenCtrl,
  output [63:0] io_out
);
  wire  _b31_T = io_immGenCtrl == 3'h6; // @[ImmGen.scala 57:39]
  wire  b31 = io_immGenCtrl == 3'h6 ? 1'h0 : io_inst[31]; // @[ImmGen.scala 57:24]
  wire  _b30to20_T = io_immGenCtrl == 3'h4; // @[ImmGen.scala 58:39]
  wire [10:0] _b30to20_T_2 = io_inst[30:20]; // @[ImmGen.scala 58:68]
  wire  _b30to20_T_3 = io_immGenCtrl == 3'h6 ? 1'h0 : io_inst[31]; // @[ImmGen.scala 58:80]
  wire [7:0] _b19to12_T_5 = io_inst[19:12]; // @[ImmGen.scala 59:110]
  wire  _b11_T_2 = io_inst[7]; // @[ImmGen.scala 60:64]
  wire  _b11_T_4 = io_immGenCtrl == 3'h5; // @[ImmGen.scala 62:43]
  wire  _b11_T_6 = io_inst[20]; // @[ImmGen.scala 62:69]
  wire  _b11_T_8 = io_immGenCtrl == 3'h5 ? $signed(_b11_T_6) : $signed(_b30to20_T_3); // @[ImmGen.scala 62:28]
  wire  _b11_T_9 = _b30to20_T ? $signed(1'sh0) : $signed(_b11_T_8); // @[ImmGen.scala 61:28]
  wire [5:0] b10to5 = _b30to20_T | _b31_T ? 6'h0 : io_inst[30:25]; // @[ImmGen.scala 63:24]
  wire  _b4to1_T = io_immGenCtrl == 3'h1; // @[ImmGen.scala 64:39]
  wire [3:0] _b4to1_T_8 = _b31_T ? io_inst[19:16] : io_inst[11:8]; // @[ImmGen.scala 66:28]
  wire [3:0] _b4to1_T_9 = _b30to20_T ? 4'h0 : _b4to1_T_8; // @[ImmGen.scala 65:28]
  wire [3:0] b4to1 = io_immGenCtrl == 3'h1 | _b11_T_4 ? io_inst[24:21] : _b4to1_T_9; // @[ImmGen.scala 64:24]
  wire  _b0_T_6 = _b31_T & io_inst[15]; // @[ImmGen.scala 69:28]
  wire  _b0_T_7 = io_immGenCtrl == 3'h2 ? io_inst[7] : _b0_T_6; // @[ImmGen.scala 68:28]
  wire  b0 = _b4to1_T ? io_inst[20] : _b0_T_7; // @[ImmGen.scala 67:24]
  wire [10:0] _io_test_b30to20_T = io_immGenCtrl == 3'h4 ? $signed(_b30to20_T_2) : $signed({11{_b30to20_T_3}}); // @[ImmGen.scala 72:32]
  wire [7:0] _io_test_b19to12_T = io_immGenCtrl != 3'h4 & io_immGenCtrl != 3'h5 ? $signed({8{_b30to20_T_3}}) : $signed(
    _b19to12_T_5); // @[ImmGen.scala 73:32]
  wire  _io_test_b11_T = io_immGenCtrl == 3'h3 ? $signed(_b11_T_2) : $signed(_b11_T_9); // @[ImmGen.scala 74:24]
  wire [31:0] _io_out_T_2 = {b31,_io_test_b30to20_T,_io_test_b19to12_T,_io_test_b11_T,b10to5,b4to1,b0}; // @[ImmGen.scala 79:73]
  assign io_out = {{32'd0}, _io_out_T_2}; // @[ImmGen.scala 79:73]
endmodule
module Control(
  input  [31:0] io_inst,
  output [2:0]  io_immGenCtrl,
  output [3:0]  io_aluCtrl,
  output        io_aluInCtrl,
  output        io_PCSrc,
  output [1:0]  io_sttype,
  output [2:0]  io_ldtype,
  output        io_wben
);
  wire [31:0] _controls_T = io_inst & 32'h707f; // @[Lookup.scala 31:38]
  wire  _controls_T_1 = 32'h3 == _controls_T; // @[Lookup.scala 31:38]
  wire  _controls_T_3 = 32'h23 == _controls_T; // @[Lookup.scala 31:38]
  wire [31:0] _controls_T_4 = io_inst & 32'hfe00707f; // @[Lookup.scala 31:38]
  wire  _controls_T_5 = 32'h33 == _controls_T_4; // @[Lookup.scala 31:38]
  wire  _controls_T_7 = 32'h40000033 == _controls_T_4; // @[Lookup.scala 31:38]
  wire  _controls_T_9 = 32'h13 == _controls_T; // @[Lookup.scala 31:38]
  wire  _controls_T_11 = 32'h6033 == _controls_T_4; // @[Lookup.scala 31:38]
  wire  _controls_T_13 = 32'h7033 == _controls_T_4; // @[Lookup.scala 31:38]
  wire  _controls_T_15 = 32'h63 == _controls_T; // @[Lookup.scala 31:38]
  wire [3:0] _controls_T_16 = _controls_T_15 ? 4'h2 : 4'hf; // @[Lookup.scala 33:37]
  wire [3:0] _controls_T_17 = _controls_T_13 ? 4'h0 : _controls_T_16; // @[Lookup.scala 33:37]
  wire [3:0] _controls_T_18 = _controls_T_11 ? 4'h1 : _controls_T_17; // @[Lookup.scala 33:37]
  wire [3:0] _controls_T_19 = _controls_T_9 ? 4'h2 : _controls_T_18; // @[Lookup.scala 33:37]
  wire [3:0] _controls_T_20 = _controls_T_7 ? 4'h6 : _controls_T_19; // @[Lookup.scala 33:37]
  wire [3:0] _controls_T_21 = _controls_T_5 ? 4'h2 : _controls_T_20; // @[Lookup.scala 33:37]
  wire [3:0] _controls_T_22 = _controls_T_3 ? 4'h2 : _controls_T_21; // @[Lookup.scala 33:37]
  wire  _controls_T_26 = _controls_T_9 ? 1'h0 : _controls_T_11 | (_controls_T_13 | _controls_T_15); // @[Lookup.scala 33:37]
  wire  _controls_T_29 = _controls_T_3 ? 1'h0 : _controls_T_5 | (_controls_T_7 | _controls_T_26); // @[Lookup.scala 33:37]
  wire [2:0] _controls_T_30 = _controls_T_15 ? 3'h3 : 3'h0; // @[Lookup.scala 33:37]
  wire [2:0] _controls_T_31 = _controls_T_13 ? 3'h0 : _controls_T_30; // @[Lookup.scala 33:37]
  wire [2:0] _controls_T_32 = _controls_T_11 ? 3'h0 : _controls_T_31; // @[Lookup.scala 33:37]
  wire [2:0] _controls_T_33 = _controls_T_9 ? 3'h1 : _controls_T_32; // @[Lookup.scala 33:37]
  wire [2:0] _controls_T_34 = _controls_T_7 ? 3'h0 : _controls_T_33; // @[Lookup.scala 33:37]
  wire [2:0] _controls_T_35 = _controls_T_5 ? 3'h0 : _controls_T_34; // @[Lookup.scala 33:37]
  wire [2:0] _controls_T_36 = _controls_T_3 ? 3'h2 : _controls_T_35; // @[Lookup.scala 33:37]
  wire  _controls_T_38 = _controls_T_13 ? 1'h0 : _controls_T_15; // @[Lookup.scala 33:37]
  wire  _controls_T_39 = _controls_T_11 ? 1'h0 : _controls_T_38; // @[Lookup.scala 33:37]
  wire  _controls_T_40 = _controls_T_9 ? 1'h0 : _controls_T_39; // @[Lookup.scala 33:37]
  wire  _controls_T_41 = _controls_T_7 ? 1'h0 : _controls_T_40; // @[Lookup.scala 33:37]
  wire  _controls_T_42 = _controls_T_5 ? 1'h0 : _controls_T_41; // @[Lookup.scala 33:37]
  wire  _controls_T_43 = _controls_T_3 ? 1'h0 : _controls_T_42; // @[Lookup.scala 33:37]
  wire [1:0] _controls_T_50 = _controls_T_3 ? 2'h3 : 2'h0; // @[Lookup.scala 33:37]
  wire  _controls_T_64 = _controls_T_3 ? 1'h0 : _controls_T_5 | (_controls_T_7 | (_controls_T_9 | (_controls_T_11 |
    _controls_T_13))); // @[Lookup.scala 33:37]
  assign io_immGenCtrl = _controls_T_1 ? 3'h1 : _controls_T_36; // @[Lookup.scala 33:37]
  assign io_aluCtrl = _controls_T_1 ? 4'h2 : _controls_T_22; // @[Lookup.scala 33:37]
  assign io_aluInCtrl = _controls_T_1 ? 1'h0 : _controls_T_29; // @[Lookup.scala 33:37]
  assign io_PCSrc = _controls_T_1 ? 1'h0 : _controls_T_43; // @[Lookup.scala 33:37]
  assign io_sttype = _controls_T_1 ? 2'h0 : _controls_T_50; // @[Lookup.scala 33:37]
  assign io_ldtype = _controls_T_1 ? 3'h4 : 3'h0; // @[Lookup.scala 33:37]
  assign io_wben = _controls_T_1 | _controls_T_64; // @[Lookup.scala 33:37]
endmodule
module RegFile(
  input         clock,
  input  [4:0]  io_raddr1,
  input  [4:0]  io_raddr2,
  input  [4:0]  io_waddr,
  input  [31:0] io_wdata,
  input         io_wen,
  output [31:0] io_rs1,
  output [31:0] io_rs2
);
`ifdef RANDOMIZE_MEM_INIT
  reg [31:0] _RAND_0;
`endif // RANDOMIZE_MEM_INIT
  reg [31:0] reg_ [0:31]; // @[RegFile.scala 25:18]
  wire [31:0] reg__io_rs1_MPORT_data; // @[RegFile.scala 25:18]
  wire [4:0] reg__io_rs1_MPORT_addr; // @[RegFile.scala 25:18]
  wire [31:0] reg__io_rs2_MPORT_data; // @[RegFile.scala 25:18]
  wire [4:0] reg__io_rs2_MPORT_addr; // @[RegFile.scala 25:18]
  wire [31:0] reg__MPORT_data; // @[RegFile.scala 25:18]
  wire [4:0] reg__MPORT_addr; // @[RegFile.scala 25:18]
  wire  reg__MPORT_mask; // @[RegFile.scala 25:18]
  wire  reg__MPORT_en; // @[RegFile.scala 25:18]
  wire  _T = |io_waddr; // @[RegFile.scala 27:29]
  assign reg__io_rs1_MPORT_addr = io_raddr1;
  assign reg__io_rs1_MPORT_data = reg_[reg__io_rs1_MPORT_addr]; // @[RegFile.scala 25:18]
  assign reg__io_rs2_MPORT_addr = io_raddr2;
  assign reg__io_rs2_MPORT_data = reg_[reg__io_rs2_MPORT_addr]; // @[RegFile.scala 25:18]
  assign reg__MPORT_data = io_wdata;
  assign reg__MPORT_addr = io_waddr;
  assign reg__MPORT_mask = 1'h1;
  assign reg__MPORT_en = io_wen & _T;
  assign io_rs1 = |io_raddr1 ? reg__io_rs1_MPORT_data : 32'h0; // @[RegFile.scala 31:18]
  assign io_rs2 = |io_raddr2 ? reg__io_rs2_MPORT_data : 32'h0; // @[RegFile.scala 32:18]
  always @(posedge clock) begin
    if(reg__MPORT_en & reg__MPORT_mask) begin
      reg_[reg__MPORT_addr] <= reg__MPORT_data; // @[RegFile.scala 25:18]
    end
  end
// Register and memory initialization
`ifdef RANDOMIZE_GARBAGE_ASSIGN
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_INVALID_ASSIGN
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_REG_INIT
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_MEM_INIT
`define RANDOMIZE
`endif
`ifndef RANDOM
`define RANDOM $random
`endif
`ifdef RANDOMIZE_MEM_INIT
  integer initvar;
`endif
`ifndef SYNTHESIS
`ifdef FIRRTL_BEFORE_INITIAL
`FIRRTL_BEFORE_INITIAL
`endif
initial begin
  `ifdef RANDOMIZE
    `ifdef INIT_RANDOM
      `INIT_RANDOM
    `endif
    `ifndef VERILATOR
      `ifdef RANDOMIZE_DELAY
        #`RANDOMIZE_DELAY begin end
      `else
        #0.002 begin end
      `endif
    `endif
`ifdef RANDOMIZE_MEM_INIT
  _RAND_0 = {1{`RANDOM}};
  for (initvar = 0; initvar < 32; initvar = initvar+1)
    reg_[initvar] = _RAND_0[31:0];
`endif // RANDOMIZE_MEM_INIT
  `endif // RANDOMIZE
end // initial
`ifdef FIRRTL_AFTER_INITIAL
`FIRRTL_AFTER_INITIAL
`endif
`endif // SYNTHESIS
endmodule
module ALU(
  input  [31:0] io_op1,
  input  [31:0] io_op2,
  input  [3:0]  io_operation,
  output        io_zero,
  output [31:0] io_result
);
  wire [31:0] and_res = io_op1 & io_op2; // @[ALU.scala 25:26]
  wire [31:0] or_res = io_op1 | io_op2; // @[ALU.scala 26:25]
  wire [31:0] add_res = io_op1 + io_op2; // @[ALU.scala 27:26]
  wire [31:0] sub_res = io_op1 - io_op2; // @[ALU.scala 28:26]
  wire [31:0] _io_result_T_3 = io_operation == 4'h2 ? add_res : sub_res; // @[ALU.scala 32:24]
  wire [31:0] _io_result_T_4 = io_operation == 4'h1 ? or_res : _io_result_T_3; // @[ALU.scala 31:24]
  assign io_zero = |sub_res ? 1'h0 : 1'h1; // @[ALU.scala 34:19]
  assign io_result = io_operation == 4'h0 ? and_res : _io_result_T_4; // @[ALU.scala 30:21]
endmodule
module Dataflow(
  input         clock,
  input         reset,
  output        io_dMemIO_req_valid,
  output [31:0] io_dMemIO_req_bits_addr,
  output [31:0] io_dMemIO_req_bits_data,
  output [3:0]  io_dMemIO_req_bits_mask,
  input         io_dMemIO_resp_valid,
  input  [31:0] io_dMemIO_resp_bits_data,
  output        io_iMemIO_req_valid,
  output [31:0] io_iMemIO_req_bits_addr,
  output [31:0] io_iMemIO_req_bits_data,
  output [3:0]  io_iMemIO_req_bits_mask,
  input         io_iMemIO_resp_valid,
  input  [31:0] io_iMemIO_resp_bits_data
);
`ifdef RANDOMIZE_REG_INIT
  reg [31:0] _RAND_0;
`endif // RANDOMIZE_REG_INIT
  wire [31:0] immGen_io_inst; // @[Dataflow.scala 28:24]
  wire [2:0] immGen_io_immGenCtrl; // @[Dataflow.scala 28:24]
  wire [63:0] immGen_io_out; // @[Dataflow.scala 28:24]
  wire [31:0] control_io_inst; // @[Dataflow.scala 29:25]
  wire [2:0] control_io_immGenCtrl; // @[Dataflow.scala 29:25]
  wire [3:0] control_io_aluCtrl; // @[Dataflow.scala 29:25]
  wire  control_io_aluInCtrl; // @[Dataflow.scala 29:25]
  wire  control_io_PCSrc; // @[Dataflow.scala 29:25]
  wire [1:0] control_io_sttype; // @[Dataflow.scala 29:25]
  wire [2:0] control_io_ldtype; // @[Dataflow.scala 29:25]
  wire  control_io_wben; // @[Dataflow.scala 29:25]
  wire  regFile_clock; // @[Dataflow.scala 30:25]
  wire [4:0] regFile_io_raddr1; // @[Dataflow.scala 30:25]
  wire [4:0] regFile_io_raddr2; // @[Dataflow.scala 30:25]
  wire [4:0] regFile_io_waddr; // @[Dataflow.scala 30:25]
  wire [31:0] regFile_io_wdata; // @[Dataflow.scala 30:25]
  wire  regFile_io_wen; // @[Dataflow.scala 30:25]
  wire [31:0] regFile_io_rs1; // @[Dataflow.scala 30:25]
  wire [31:0] regFile_io_rs2; // @[Dataflow.scala 30:25]
  wire [31:0] alu_io_op1; // @[Dataflow.scala 31:21]
  wire [31:0] alu_io_op2; // @[Dataflow.scala 31:21]
  wire [3:0] alu_io_operation; // @[Dataflow.scala 31:21]
  wire  alu_io_zero; // @[Dataflow.scala 31:21]
  wire [31:0] alu_io_result; // @[Dataflow.scala 31:21]
  reg [31:0] pc; // @[Dataflow.scala 33:21]
  wire [63:0] _alu_io_op2_T_1 = ~control_io_aluInCtrl ? immGen_io_out : {{32'd0}, regFile_io_rs2}; // @[Dataflow.scala 62:22]
  wire [63:0] _GEN_0 = {{32'd0}, pc}; // @[Dataflow.scala 69:32]
  wire [63:0] tBranchaddr = immGen_io_out + _GEN_0; // @[Dataflow.scala 69:32]
  wire [31:0] _mpc_T_3 = pc + 32'h4; // @[Dataflow.scala 72:77]
  wire [63:0] mpc = control_io_PCSrc & alu_io_zero ? tBranchaddr : {{32'd0}, _mpc_T_3}; // @[Dataflow.scala 72:18]
  wire [1:0] moffset = alu_io_result[1:0]; // @[Dataflow.scala 81:32]
  wire [4:0] doffset = {moffset, 3'h0}; // @[Dataflow.scala 82:27]
  wire [62:0] _GEN_1 = {{31'd0}, regFile_io_rs2}; // @[Dataflow.scala 84:36]
  wire [62:0] _io_dMemIO_req_bits_data_T = _GEN_1 << doffset; // @[Dataflow.scala 84:36]
  wire [4:0] _io_dMemIO_req_bits_mask_T = 5'h3 << moffset; // @[Dataflow.scala 88:39]
  wire [3:0] _io_dMemIO_req_bits_mask_T_1 = 4'h1 << moffset; // @[Dataflow.scala 89:38]
  wire [3:0] _io_dMemIO_req_bits_mask_T_3 = 2'h1 == control_io_sttype ? 4'hf : 4'h0; // @[Mux.scala 80:57]
  wire [4:0] _io_dMemIO_req_bits_mask_T_5 = 2'h2 == control_io_sttype ? _io_dMemIO_req_bits_mask_T : {{1'd0},
    _io_dMemIO_req_bits_mask_T_3}; // @[Mux.scala 80:57]
  wire [4:0] _io_dMemIO_req_bits_mask_T_7 = 2'h3 == control_io_sttype ? {{1'd0}, _io_dMemIO_req_bits_mask_T_1} :
    _io_dMemIO_req_bits_mask_T_5; // @[Mux.scala 80:57]
  wire  _io_dMemIO_req_valid_T_1 = |control_io_ldtype; // @[Dataflow.scala 91:71]
  wire [31:0] _memrdata_T = io_dMemIO_resp_valid ? io_dMemIO_resp_bits_data : 32'h0; // @[Dataflow.scala 93:23]
  wire [31:0] memrdata = _memrdata_T >> doffset; // @[Dataflow.scala 93:77]
  wire [31:0] _rdata_T = _memrdata_T >> doffset; // @[Dataflow.scala 94:55]
  wire [15:0] _rdata_T_2 = memrdata[15:0]; // @[Dataflow.scala 96:47]
  wire [16:0] _rdata_T_4 = {1'b0,$signed(memrdata[15:0])}; // @[Dataflow.scala 97:48]
  wire [7:0] _rdata_T_6 = memrdata[7:0]; // @[Dataflow.scala 98:46]
  wire [8:0] _rdata_T_8 = {1'b0,$signed(memrdata[7:0])}; // @[Dataflow.scala 99:47]
  wire [31:0] _rdata_T_10 = 3'h2 == control_io_ldtype ? $signed({{16{_rdata_T_2[15]}},_rdata_T_2}) : $signed(_rdata_T); // @[Mux.scala 80:57]
  wire [31:0] _rdata_T_12 = 3'h3 == control_io_ldtype ? $signed({{15{_rdata_T_4[16]}},_rdata_T_4}) : $signed(_rdata_T_10
    ); // @[Mux.scala 80:57]
  wire [31:0] _rdata_T_14 = 3'h4 == control_io_ldtype ? $signed({{24{_rdata_T_6[7]}},_rdata_T_6}) : $signed(_rdata_T_12)
    ; // @[Mux.scala 80:57]
  wire [31:0] _regFile_io_wdata_T_1 = 3'h5 == control_io_ldtype ? $signed({{23{_rdata_T_8[8]}},_rdata_T_8}) : $signed(
    _rdata_T_14); // @[Dataflow.scala 105:58]
  ImmGen immGen ( // @[Dataflow.scala 28:24]
    .io_inst(immGen_io_inst),
    .io_immGenCtrl(immGen_io_immGenCtrl),
    .io_out(immGen_io_out)
  );
  Control control ( // @[Dataflow.scala 29:25]
    .io_inst(control_io_inst),
    .io_immGenCtrl(control_io_immGenCtrl),
    .io_aluCtrl(control_io_aluCtrl),
    .io_aluInCtrl(control_io_aluInCtrl),
    .io_PCSrc(control_io_PCSrc),
    .io_sttype(control_io_sttype),
    .io_ldtype(control_io_ldtype),
    .io_wben(control_io_wben)
  );
  RegFile regFile ( // @[Dataflow.scala 30:25]
    .clock(regFile_clock),
    .io_raddr1(regFile_io_raddr1),
    .io_raddr2(regFile_io_raddr2),
    .io_waddr(regFile_io_waddr),
    .io_wdata(regFile_io_wdata),
    .io_wen(regFile_io_wen),
    .io_rs1(regFile_io_rs1),
    .io_rs2(regFile_io_rs2)
  );
  ALU alu ( // @[Dataflow.scala 31:21]
    .io_op1(alu_io_op1),
    .io_op2(alu_io_op2),
    .io_operation(alu_io_operation),
    .io_zero(alu_io_zero),
    .io_result(alu_io_result)
  );
  assign io_dMemIO_req_valid = |control_io_sttype | |control_io_ldtype; // @[Dataflow.scala 91:50]
  assign io_dMemIO_req_bits_addr = alu_io_result; // @[Dataflow.scala 79:29]
  assign io_dMemIO_req_bits_data = _io_dMemIO_req_bits_data_T[31:0]; // @[Dataflow.scala 84:29]
  assign io_dMemIO_req_bits_mask = _io_dMemIO_req_bits_mask_T_7[3:0]; // @[Dataflow.scala 85:29]
  assign io_iMemIO_req_valid = 1'h1; // @[Dataflow.scala 39:25]
  assign io_iMemIO_req_bits_addr = pc; // @[Dataflow.scala 38:29]
  assign io_iMemIO_req_bits_data = 32'h0;
  assign io_iMemIO_req_bits_mask = 4'h0; // @[Dataflow.scala 40:29]
  assign immGen_io_inst = io_iMemIO_resp_bits_data; // @[Dataflow.scala 49:20]
  assign immGen_io_immGenCtrl = control_io_immGenCtrl; // @[Dataflow.scala 50:26]
  assign control_io_inst = io_iMemIO_resp_bits_data; // @[Dataflow.scala 45:21]
  assign regFile_clock = clock;
  assign regFile_io_raddr1 = io_iMemIO_resp_bits_data[19:15]; // @[Dataflow.scala 54:30]
  assign regFile_io_raddr2 = io_iMemIO_resp_bits_data[24:20]; // @[Dataflow.scala 55:30]
  assign regFile_io_waddr = io_iMemIO_resp_bits_data[11:7]; // @[Dataflow.scala 103:29]
  assign regFile_io_wdata = _io_dMemIO_req_valid_T_1 ? _regFile_io_wdata_T_1 : alu_io_result; // @[Dataflow.scala 105:28]
  assign regFile_io_wen = control_io_wben; // @[Dataflow.scala 104:20]
  assign alu_io_op1 = regFile_io_rs1; // @[Dataflow.scala 61:16]
  assign alu_io_op2 = _alu_io_op2_T_1[31:0]; // @[Dataflow.scala 62:16]
  assign alu_io_operation = control_io_aluCtrl; // @[Dataflow.scala 60:22]
  always @(posedge clock) begin
    if (reset) begin // @[Dataflow.scala 33:21]
      pc <= 32'h0; // @[Dataflow.scala 33:21]
    end else begin
      pc <= mpc[31:0]; // @[Dataflow.scala 75:8]
    end
  end
// Register and memory initialization
`ifdef RANDOMIZE_GARBAGE_ASSIGN
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_INVALID_ASSIGN
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_REG_INIT
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_MEM_INIT
`define RANDOMIZE
`endif
`ifndef RANDOM
`define RANDOM $random
`endif
`ifdef RANDOMIZE_MEM_INIT
  integer initvar;
`endif
`ifndef SYNTHESIS
`ifdef FIRRTL_BEFORE_INITIAL
`FIRRTL_BEFORE_INITIAL
`endif
initial begin
  `ifdef RANDOMIZE
    `ifdef INIT_RANDOM
      `INIT_RANDOM
    `endif
    `ifndef VERILATOR
      `ifdef RANDOMIZE_DELAY
        #`RANDOMIZE_DELAY begin end
      `else
        #0.002 begin end
      `endif
    `endif
`ifdef RANDOMIZE_REG_INIT
  _RAND_0 = {1{`RANDOM}};
  pc = _RAND_0[31:0];
`endif // RANDOMIZE_REG_INIT
  `endif // RANDOMIZE
end // initial
`ifdef FIRRTL_AFTER_INITIAL
`FIRRTL_AFTER_INITIAL
`endif
`endif // SYNTHESIS
endmodule
