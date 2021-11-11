addi x4, x0, 0x5    # 5
addi x13, x0, 0x5   # 5
addi x1, x0, 0x10   # 16
add x4, x1, x0      # 16
addi x5, x4, 0x16   # 38
add x8, x4, x5      # 54
add x6, x1, x4      # 32
sw  x6, 0x6(x8)     # 60
lw  x11, 0x6(x8)    # 32
lw  x9, 0x6(x8)     # 32
add x10, x9, x0     # 32
add x12, x9, x10    # 64
add x7, x13, x12    # 69
sw  x7, 0x10(x10)   # 48

addi x1, x0, 0x6    # 6
addi x2, x0, 0x5    # 5
beq  x1, x2, _equal # 5
addi x2, x2, 0x1    # 6
beq  x1, x2, _equal # 6
addi x2, x2, 0x1
addi x2, x2, 0x1
addi x2, x2, 0x1
addi x2, x2, 0x1
_equal:
sw   x2, 0x12(x0)    # 18
jal  x4, _jump       
addi x4, x4, 0x6
addi x4, x4, 0x6
addi x4, x4, 0x6
addi x4, x4, 0x6
addi x4, x4, 0x6
_jump:
sw   x4, 0x12(x0)    # 18
