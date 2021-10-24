module Memory(
  input         clock,
  input         reset,
  input         io_req_valid,
  input  [31:0] io_req_bits_addr,
  input  [31:0] io_req_bits_data,
  input  [3:0]  io_req_bits_mask,
  output        io_resp_valid,
  output [31:0] io_resp_bits_data
);
`ifdef RANDOMIZE_MEM_INIT
  reg [31:0] _RAND_0;
  reg [31:0] _RAND_3;
  reg [31:0] _RAND_6;
  reg [31:0] _RAND_9;
`endif // RANDOMIZE_MEM_INIT
`ifdef RANDOMIZE_REG_INIT
  reg [31:0] _RAND_1;
  reg [31:0] _RAND_2;
  reg [31:0] _RAND_4;
  reg [31:0] _RAND_5;
  reg [31:0] _RAND_7;
  reg [31:0] _RAND_8;
  reg [31:0] _RAND_10;
  reg [31:0] _RAND_11;
`endif // RANDOMIZE_REG_INIT
  reg [7:0] mem_0 [0:255]; // @[Memory.scala 37:26]
  wire [7:0] mem_0_data_1_data; // @[Memory.scala 37:26]
  wire [7:0] mem_0_data_1_addr; // @[Memory.scala 37:26]
  wire [7:0] mem_0_MPORT_data; // @[Memory.scala 37:26]
  wire [7:0] mem_0_MPORT_addr; // @[Memory.scala 37:26]
  wire  mem_0_MPORT_mask; // @[Memory.scala 37:26]
  wire  mem_0_MPORT_en; // @[Memory.scala 37:26]
  reg  mem_0_data_1_en_pipe_0;
  reg [7:0] mem_0_data_1_addr_pipe_0;
  reg [7:0] mem_1 [0:255]; // @[Memory.scala 37:26]
  wire [7:0] mem_1_data_1_data; // @[Memory.scala 37:26]
  wire [7:0] mem_1_data_1_addr; // @[Memory.scala 37:26]
  wire [7:0] mem_1_MPORT_data; // @[Memory.scala 37:26]
  wire [7:0] mem_1_MPORT_addr; // @[Memory.scala 37:26]
  wire  mem_1_MPORT_mask; // @[Memory.scala 37:26]
  wire  mem_1_MPORT_en; // @[Memory.scala 37:26]
  reg  mem_1_data_1_en_pipe_0;
  reg [7:0] mem_1_data_1_addr_pipe_0;
  reg [7:0] mem_2 [0:255]; // @[Memory.scala 37:26]
  wire [7:0] mem_2_data_1_data; // @[Memory.scala 37:26]
  wire [7:0] mem_2_data_1_addr; // @[Memory.scala 37:26]
  wire [7:0] mem_2_MPORT_data; // @[Memory.scala 37:26]
  wire [7:0] mem_2_MPORT_addr; // @[Memory.scala 37:26]
  wire  mem_2_MPORT_mask; // @[Memory.scala 37:26]
  wire  mem_2_MPORT_en; // @[Memory.scala 37:26]
  reg  mem_2_data_1_en_pipe_0;
  reg [7:0] mem_2_data_1_addr_pipe_0;
  reg [7:0] mem_3 [0:255]; // @[Memory.scala 37:26]
  wire [7:0] mem_3_data_1_data; // @[Memory.scala 37:26]
  wire [7:0] mem_3_data_1_addr; // @[Memory.scala 37:26]
  wire [7:0] mem_3_MPORT_data; // @[Memory.scala 37:26]
  wire [7:0] mem_3_MPORT_addr; // @[Memory.scala 37:26]
  wire  mem_3_MPORT_mask; // @[Memory.scala 37:26]
  wire  mem_3_MPORT_en; // @[Memory.scala 37:26]
  reg  mem_3_data_1_en_pipe_0;
  reg [7:0] mem_3_data_1_addr_pipe_0;
  wire [31:0] aligned_addr = {{2'd0}, io_req_bits_addr[31:2]}; // @[Memory.scala 28:42]
  wire  _wen_T = |io_req_bits_mask; // @[Memory.scala 32:32]
  wire  wen = |io_req_bits_mask & io_req_valid; // @[Memory.scala 32:36]
  wire  ren = io_req_valid & ~wen; // @[Memory.scala 33:28]
  wire [15:0] io_resp_bits_data_lo = {mem_1_data_1_data,mem_0_data_1_data}; // @[Cat.scala 30:58]
  wire [15:0] io_resp_bits_data_hi = {mem_3_data_1_data,mem_2_data_1_data}; // @[Cat.scala 30:58]
  assign mem_0_data_1_addr = mem_0_data_1_addr_pipe_0;
  assign mem_0_data_1_data = mem_0[mem_0_data_1_addr]; // @[Memory.scala 37:26]
  assign mem_0_MPORT_data = io_req_bits_data[7:0];
  assign mem_0_MPORT_addr = aligned_addr[7:0];
  assign mem_0_MPORT_mask = io_req_bits_mask[0];
  assign mem_0_MPORT_en = _wen_T & io_req_valid;
  assign mem_1_data_1_addr = mem_1_data_1_addr_pipe_0;
  assign mem_1_data_1_data = mem_1[mem_1_data_1_addr]; // @[Memory.scala 37:26]
  assign mem_1_MPORT_data = io_req_bits_data[15:8];
  assign mem_1_MPORT_addr = aligned_addr[7:0];
  assign mem_1_MPORT_mask = io_req_bits_mask[1];
  assign mem_1_MPORT_en = _wen_T & io_req_valid;
  assign mem_2_data_1_addr = mem_2_data_1_addr_pipe_0;
  assign mem_2_data_1_data = mem_2[mem_2_data_1_addr]; // @[Memory.scala 37:26]
  assign mem_2_MPORT_data = io_req_bits_data[23:16];
  assign mem_2_MPORT_addr = aligned_addr[7:0];
  assign mem_2_MPORT_mask = io_req_bits_mask[2];
  assign mem_2_MPORT_en = _wen_T & io_req_valid;
  assign mem_3_data_1_addr = mem_3_data_1_addr_pipe_0;
  assign mem_3_data_1_data = mem_3[mem_3_data_1_addr]; // @[Memory.scala 37:26]
  assign mem_3_MPORT_data = io_req_bits_data[31:24];
  assign mem_3_MPORT_addr = aligned_addr[7:0];
  assign mem_3_MPORT_mask = io_req_bits_mask[3];
  assign mem_3_MPORT_en = _wen_T & io_req_valid;
  assign io_resp_valid = wen ? 1'h0 : ren; // @[Memory.scala 40:14 Memory.scala 34:19]
  assign io_resp_bits_data = {io_resp_bits_data_hi,io_resp_bits_data_lo}; // @[Cat.scala 30:58]
  always @(posedge clock) begin
    if(mem_0_MPORT_en & mem_0_MPORT_mask) begin
      mem_0[mem_0_MPORT_addr] <= mem_0_MPORT_data; // @[Memory.scala 37:26]
    end
    if (wen) begin
      mem_0_data_1_en_pipe_0 <= 1'h0;
    end else begin
      mem_0_data_1_en_pipe_0 <= ren;
    end
    if (wen ? 1'h0 : ren) begin
      mem_0_data_1_addr_pipe_0 <= aligned_addr[7:0];
    end
    if(mem_1_MPORT_en & mem_1_MPORT_mask) begin
      mem_1[mem_1_MPORT_addr] <= mem_1_MPORT_data; // @[Memory.scala 37:26]
    end
    if (wen) begin
      mem_1_data_1_en_pipe_0 <= 1'h0;
    end else begin
      mem_1_data_1_en_pipe_0 <= ren;
    end
    if (wen ? 1'h0 : ren) begin
      mem_1_data_1_addr_pipe_0 <= aligned_addr[7:0];
    end
    if(mem_2_MPORT_en & mem_2_MPORT_mask) begin
      mem_2[mem_2_MPORT_addr] <= mem_2_MPORT_data; // @[Memory.scala 37:26]
    end
    if (wen) begin
      mem_2_data_1_en_pipe_0 <= 1'h0;
    end else begin
      mem_2_data_1_en_pipe_0 <= ren;
    end
    if (wen ? 1'h0 : ren) begin
      mem_2_data_1_addr_pipe_0 <= aligned_addr[7:0];
    end
    if(mem_3_MPORT_en & mem_3_MPORT_mask) begin
      mem_3[mem_3_MPORT_addr] <= mem_3_MPORT_data; // @[Memory.scala 37:26]
    end
    if (wen) begin
      mem_3_data_1_en_pipe_0 <= 1'h0;
    end else begin
      mem_3_data_1_en_pipe_0 <= ren;
    end
    if (wen ? 1'h0 : ren) begin
      mem_3_data_1_addr_pipe_0 <= aligned_addr[7:0];
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
  for (initvar = 0; initvar < 256; initvar = initvar+1)
    mem_0[initvar] = _RAND_0[7:0];
  _RAND_3 = {1{`RANDOM}};
  for (initvar = 0; initvar < 256; initvar = initvar+1)
    mem_1[initvar] = _RAND_3[7:0];
  _RAND_6 = {1{`RANDOM}};
  for (initvar = 0; initvar < 256; initvar = initvar+1)
    mem_2[initvar] = _RAND_6[7:0];
  _RAND_9 = {1{`RANDOM}};
  for (initvar = 0; initvar < 256; initvar = initvar+1)
    mem_3[initvar] = _RAND_9[7:0];
`endif // RANDOMIZE_MEM_INIT
`ifdef RANDOMIZE_REG_INIT
  _RAND_1 = {1{`RANDOM}};
  mem_0_data_1_en_pipe_0 = _RAND_1[0:0];
  _RAND_2 = {1{`RANDOM}};
  mem_0_data_1_addr_pipe_0 = _RAND_2[7:0];
  _RAND_4 = {1{`RANDOM}};
  mem_1_data_1_en_pipe_0 = _RAND_4[0:0];
  _RAND_5 = {1{`RANDOM}};
  mem_1_data_1_addr_pipe_0 = _RAND_5[7:0];
  _RAND_7 = {1{`RANDOM}};
  mem_2_data_1_en_pipe_0 = _RAND_7[0:0];
  _RAND_8 = {1{`RANDOM}};
  mem_2_data_1_addr_pipe_0 = _RAND_8[7:0];
  _RAND_10 = {1{`RANDOM}};
  mem_3_data_1_en_pipe_0 = _RAND_10[0:0];
  _RAND_11 = {1{`RANDOM}};
  mem_3_data_1_addr_pipe_0 = _RAND_11[7:0];
`endif // RANDOMIZE_REG_INIT
  `endif // RANDOMIZE
end // initial
`ifdef FIRRTL_AFTER_INITIAL
`FIRRTL_AFTER_INITIAL
`endif
`endif // SYNTHESIS
endmodule
