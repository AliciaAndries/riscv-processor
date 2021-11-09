nop
nop
addi x4, x0, 0x5    # 5
addi x13, x0, 0x5    # 5
addi x1, x0, 0x10   # 16
add x4, x1, x0      # 16
addi x5, x4, 0x16   # 38
add x8, x4, x5      # 54
add x6, x1, x4      # 32
sw  x6, 0x6(x8)     # 60
lw  x9, 0x6(x8)     # 32
sw  x4, 0x6(x8)     # 60
lw  x11, 0x6(x8)     # 32
sw  x5, 0x6(x8)     # 60
lw  x11, 0x2(x8)     # 32
sw  x8, 0x6(x8)     # 60
lw  x11, 0x6(x8)     # 32
sw  x13, 0x6(x8)     # 60
lw  x11, 0x6(x8)     # 32
add x10, x9, x0	    # 32
add x10, x11, x0    # 32
addi x7, x0, 0xA    # 10
