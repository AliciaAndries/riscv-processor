_start:
    addi x1, x0, 0x01   # init reg that says if there is a branch taken
    addi x2, x0, 0xE1   # write 225 to x2
    addi x3, x0, 0xA    # write 10 to x3
    addi x16, x0, 0x18
    addi x20, x0, 0x35  # clkdevide should be 21

_arythmatic:
    add  x4, x2, x3     # 225 + 10 = 235
    sub  x5, x4, x3     # 235 - 10 = 225
    srl  x6, x5, x1     # x5 >> x1(4,0) = 112   
    or   x7, x6, x2     # 241
    xor  x8, x2, x1     # 224
    slli x9, x8, 0x18   # 224 << 24 = 3758096384

_loop_start:
    bge  x5, x7, _another_loop   # 241
    sll  x7, x7, x16		# 4043309056
    jal  x0, _loop_start    

_another_loop: 
    bltu x7, x5, _third_loop
    srli x7, x7, 0x19   # 120
    bgeu x5, x7, _another_loop

_third_loop:
    beq  x8, x5, _loop_four     # 225
    addi x8, x8, 0x01           # 225
    jal  x0, _third_loop        

_loop_four:
    bne  x8, x5, _exit
    addi x8, x8, 0x01   # 226
    blt  x5, x8, _loop_four

_exit:
    sw   x20, 0x408(x0)

    sh  x5,  0x3D(x3)   # store 225 at address 71 -> store in upper 2 bytes of 17
    lw  x10, 0x3C(x3)   # get from address 70 aka -> get whole thing at address of 68 = (255 << 16) 14745600
    sw   x5, 0x404(x0)  #uart 225


    sw  x9,  0x1E(x5)   # store 3758096384 in address 255
    lhu x11, 0x1D(x5)   # read first 2 bytes of 254 (at 253) = 57344
    lh  x14, 0x1D(x5)   # 4294959104
    sw   x14, 0x404(x0) #uart 0


    and x15, x11, x14   # 57344
    sw   x15, 0x404(x0) #uart 0
    sub x15, x9, x11    # 3758039040
    sw   x15, 0x404(x0) #uart 0
    sra x15, x14, x3    # 4294967288
    sw   x15, 0x404(x0) #uart 0
    ori x16, x9, 0x10   # #uart 0
    sw   x16, 0x404(x0) #uart 0
    xori x16, x16, 0X10 # 3758096384
    andi x17, x16, 0x10 # 0
    sw   x17, 0x404(x0) #uart 0
    srai x16, x16, 0x11 # 4294963200

    lbu x16, 0x1E(x5)   # 224
    sb  x8, 0x2E(x5)    # store 226 in address 255
    lb  x14, 0x2E(x5)   # 4294967266
    sw   x16, 0x404(x0) #uart 0

    sltiu x15, x14, 0x01 # 0  
    sw    x15, 0x404(x0) #uart 0
    slti  x15, x14, 0x01 # 1
    sw    x15, 0x404(x0) #uart 1
    slt   x15, x14, x3   # 1
    sw    x15, 0x404(x0) #uart 1
    sltu  x15, x14, x3   # 0
    sw    x15, 0x404(x0) #uart 0

    lui   x12, 0x19     # 19 << 12 = 102400
    auipc x13, 0x01     # pc_aiupc + 4096
    sw    x13, 0x404(x0)#uart 224

    Jalr x0, x13, 0x10  # 16 + pc_aiupc + 4096
    nop                 # pc =  pc_aiupc + 4096 + 16
