;buildInfoPackage: chisel3, version: 3.4.3, scalaVersion: 2.12.12, sbtVersion: 1.3.10
circuit Memory : 
  module Memory : 
    input clock : Clock
    input reset : UInt<1>
    output io : {flip req : {valid : UInt<1>, bits : {addr : UInt<32>, data : UInt<32>, mask : UInt<4>}}, resp : {valid : UInt<1>, bits : {data : UInt<32>}}}
    
    node aligned_addr = dshr(io.req.bits.addr, UInt<2>("h02")) @[Memory.scala 28:42]
    node valid_addr = bits(aligned_addr, 7, 0) @[Memory.scala 29:34]
    node _data_T = bits(io.req.bits.data, 7, 0) @[Memory.scala 31:40]
    node _data_T_1 = bits(io.req.bits.data, 15, 8) @[Memory.scala 31:63]
    node _data_T_2 = bits(io.req.bits.data, 23, 16) @[Memory.scala 31:87]
    node _data_T_3 = bits(io.req.bits.data, 31, 24) @[Memory.scala 31:112]
    wire data : UInt<8>[4] @[Memory.scala 31:23]
    data[0] <= _data_T @[Memory.scala 31:23]
    data[1] <= _data_T_1 @[Memory.scala 31:23]
    data[2] <= _data_T_2 @[Memory.scala 31:23]
    data[3] <= _data_T_3 @[Memory.scala 31:23]
    node _wen_T = orr(io.req.bits.mask) @[Memory.scala 32:32]
    node wen = and(_wen_T, io.req.valid) @[Memory.scala 32:36]
    node _ren_T = eq(wen, UInt<1>("h00")) @[Memory.scala 33:31]
    node ren = and(io.req.valid, _ren_T) @[Memory.scala 33:28]
    io.resp.valid <= UInt<1>("h00") @[Memory.scala 34:19]
    io.resp.bits.data is invalid @[Memory.scala 35:23]
    smem mem : UInt<8>[4][256], undefined @[Memory.scala 37:26]
    when wen : @[Memory.scala 40:14]
      node _T = bits(io.req.bits.mask, 0, 0) @[Memory.scala 41:54]
      node _T_1 = bits(io.req.bits.mask, 1, 1) @[Memory.scala 41:54]
      node _T_2 = bits(io.req.bits.mask, 2, 2) @[Memory.scala 41:54]
      node _T_3 = bits(io.req.bits.mask, 3, 3) @[Memory.scala 41:54]
      write mport MPORT = mem[valid_addr], clock
      when _T :
        MPORT[0] <= data[0]
        skip
      when _T_1 :
        MPORT[1] <= data[1]
        skip
      when _T_2 :
        MPORT[2] <= data[2]
        skip
      when _T_3 :
        MPORT[3] <= data[3]
        skip
      skip @[Memory.scala 40:14]
    else : @[Memory.scala 42:21]
      when ren : @[Memory.scala 42:21]
        wire _data_WIRE : UInt @[Memory.scala 43:28]
        _data_WIRE is invalid @[Memory.scala 43:28]
        when ren : @[Memory.scala 43:28]
          _data_WIRE <= valid_addr @[Memory.scala 43:28]
          node _data_T_4 = or(_data_WIRE, UInt<8>("h00")) @[Memory.scala 43:28]
          node _data_T_5 = bits(_data_T_4, 7, 0) @[Memory.scala 43:28]
          read mport data_1 = mem[_data_T_5], clock @[Memory.scala 43:28]
          skip @[Memory.scala 43:28]
        node io_resp_bits_data_lo = cat(data_1[1], data_1[0]) @[Cat.scala 30:58]
        node io_resp_bits_data_hi = cat(data_1[3], data_1[2]) @[Cat.scala 30:58]
        node _io_resp_bits_data_T = cat(io_resp_bits_data_hi, io_resp_bits_data_lo) @[Cat.scala 30:58]
        io.resp.bits.data <= _io_resp_bits_data_T @[Memory.scala 45:27]
        io.resp.valid <= UInt<1>("h01") @[Memory.scala 46:23]
        skip @[Memory.scala 42:21]
    
