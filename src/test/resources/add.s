_start:
    nop
    addi x1, x0, 0x01   # init reg that says if there is a branch taken
    addi x2, x0, 0xE1   # write 225 to x2
    addi x3, x0, 0xA    # write 10 to x3

_arythmatic:
    add  x4, x2, x3     # 225 + 10 = 235
    sub  x5, x4, x3     # 235 - 10 = 225
    srl  x6, x4, x1     # x4 >> x1(4,0) = 117
    or   x7, x4, x3     # 235
    xor  x8, x2, x1     # 224
    slli x9, x8, 0x18   # 224 << 24 = 3758096384

_loop_start:
    bge  x5, x2, _another_loop   
    sll  x2, x2, 0x18		# 3774873600
    jal  x0, _loop_start

_another_loop: 
    bltu x7, x5, _last_loop
    srli x7, x7, 0x18   # 0
    jal  x0, _another_loop

_last_loop:
    beq  x8, x5, _exit
    addi x8, x8, 0x01   # 225
    jal  x0, _last_loop

_exit:
    sh  x5,  0x3D(x3)   # store 225 at address 71 -> store in upper 2 bytes of 17
    lw  x10, 0x3C(x3)   # get from address 70 aka -> get whole thing at address of 68 = (255 << 16) 14745600
    lw  x0,  0x00(x0)

    sw  x3,  0x1E(x5)   # store 10 in address 255
    lhu x11, 0x1C(x5)   # read first 2 bytes of 252 (at 253) = 10
    lhu x0,  0x00(x0)   

    lui   x12, 0x19     # 19 << 12 = 102400
    auipc x13, 0x01     # pc_aiupc + 4096

    Jalr x0, x13, 0x10  # 16 + pc_aiupc + 4096
    nop                 # pc =  pc_aiupc + 4096 + 16
