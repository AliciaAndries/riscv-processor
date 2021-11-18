nop
addi x3, x0, 0x3
addi x4, x0, 0x15
addi x5, x0, 0x5
addi x6, x0, 0xE1
addi x8, x0, 0xE0

sw   x3, 0x400(x0)
sw   x4, 0x408(x0)
sw   x3, 0x404(x0)
sw   x5, 0x404(x0)
sw   x3, 0x404(x0)
sw   x5, 0x404(x0)

sw   x6, 0x404(x0)
sw   x8, 0x404(x0)

_third_loop:
    beq  x8, x6, _continue     # 225
    addi x8, x8, 0x01           # 225
    sw   x8, 0x404(x0)
    sw   x6, 0x404(x0)
    jal  x0, _third_loop  
_continue:
addi x8, x8, 0x01

sw x8,  0x404(x0)   #uart 226

sb  x6,  0x3C(x3)
lb  x10, 0x3C(x3)   
sw  x10, 0x404(x0)  #uart 225
