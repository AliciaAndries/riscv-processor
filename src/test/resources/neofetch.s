alicia:                                 # @alicia
        addi    a0, zero, 26
        sb      a0, 1032(zero)
        addi    a1, zero, 72
        sb      a1, 1028(zero)
        addi    a2, zero, 101
        sb      a2, 1028(zero)
        addi    a3, zero, 108
        sb      a3, 1028(zero)
        sb      a3, 1028(zero)
        addi    a4, zero, 111
        sb      a4, 1028(zero)
        addi    a0, zero, 32
        sb      a0, 1028(zero)
        addi    a5, zero, 119
        sb      a5, 1028(zero)
        sb      a4, 1028(zero)
        addi    a5, zero, 114
        sb      a5, 1028(zero)
        sb      a3, 1028(zero)
        addi    a3, zero, 100
        sb      a3, 1028(zero)
        addi    a6, zero, 33
        sb      a6, 1028(zero)
        addi    a3, zero, 10
        sb      a3, 1028(zero)
        sb      a1, 1028(zero)
        sb      a2, 1028(zero)
        sb      a5, 1028(zero)
        sb      a2, 1028(zero)
        sb      a0, 1028(zero)
        addi    a1, zero, 99
        sb      a1, 1028(zero)
        sb      a4, 1028(zero)
        addi    a1, zero, 109
        sb      a1, 1028(zero)
        sb      a2, 1028(zero)
        addi    a1, zero, 115
        sb      a1, 1028(zero)
        sb      a0, 1028(zero)
        addi    a1, zero, 116
        sb      a1, 1028(zero)
        addi    a1, zero, 117
        sb      a1, 1028(zero)
        addi    a1, zero, 120
        sb      a1, 1028(zero)
        sb      a6, 1028(zero)
        sb      a3, 1028(zero)
.LBB0_5:                                # Label of block must be emitted
        auipc   a1, %pcrel_hi(tux)
        addi    a1, a1, %pcrel_lo(.LBB0_5)
        addi    a1, a1, 1
.LBB0_1:                                # =>This Inner Loop Header: Depth=1
        sb      a0, 1028(zero)
        lbu     a0, 0(a1)
        addi    a1, a1, 1
        bnez    a0, .LBB0_1
        addi    a0, zero, 97
        sb      a0, 1028(zero)
        addi    a1, zero, 108
        sb      a1, 1028(zero)
        addi    a1, zero, 105
        sb      a1, 1028(zero)
        addi    a2, zero, 99
        sb      a2, 1028(zero)
        sb      a1, 1028(zero)
        sb      a0, 1028(zero)
        addi    a0, zero, 64
        sb      a0, 1028(zero)
        addi    a0, zero, 100
        sb      a0, 1028(zero)
        addi    a1, zero, 101
        sb      a1, 1028(zero)
        addi    a0, zero, 109
        sb      a0, 1028(zero)
        addi    a3, zero, 111
        sb      a3, 1028(zero)
        addi    a0, zero, 32
        sb      a0, 1028(zero)
        addi    a4, zero, 126
        sb      a4, 1028(zero)
        addi    a4, zero, 62
        sb      a4, 1028(zero)
        sb      a0, 1028(zero)
        addi    a4, zero, 110
        sb      a4, 1028(zero)
        sb      a1, 1028(zero)
        sb      a3, 1028(zero)
        addi    a3, zero, 102
        sb      a3, 1028(zero)
        sb      a1, 1028(zero)
        addi    a1, zero, 116
        sb      a1, 1028(zero)
        sb      a2, 1028(zero)
        addi    a1, zero, 104
        sb      a1, 1028(zero)
        addi    a1, zero, 10
        sb      a1, 1028(zero)
.LBB0_6:                                # Label of block must be emitted
        auipc   a1, %pcrel_hi(neofetch)
        addi    a1, a1, %pcrel_lo(.LBB0_6)
        addi    a1, a1, 1
.LBB0_3:                                # =>This Inner Loop Header: Depth=1
        sb      a0, 1028(zero)
        lbu     a0, 0(a1)
        addi    a1, a1, 1
        bnez    a0, .LBB0_3
        ret
tux:
        .asciz  "                 .88888888:.\n                88888888.88888.\n              .8888888888888888.\n              888888888888888888\n              88' _`88'_  `88888\n              88 88 88 88  88888\n              88_88_::_88_:88888\n              88:::,::,:::::8888\n              88`:::::::::'`8888\n             .88  `::::'    8:88.\n            8888            `8:888.\n          .8888'             `888888.\n         .8888:..  .::.  ...:'8888888:.\n        .8888.'     :'     `'::`88:88888\n       .8888        '         `.888:8888.\n      888:8         .           888:88888\n    .888:88        .:           888:88888:\n    8888888.       ::           88:888888\n    `.::.888.      ::          .88888888\n   .::::::.888.    ::         :::`8888'.:.\n  ::::::::::.888   '         .::::::::::::\n  ::::::::::::.8    '      .:8::::::::::::.\n .::::::::::::::.        .:888:::::::::::::\n :::::::::::::::88:.__..:88888:::::::::::'\n  `'.:::::::::::88888888888.88:::::::::'\n        `':::_:' -- '' -'-' `':_::::'`\n"

neofetch:
        .asciz  "             /////////////                alicia@demo\n         /////////////////////            ---------------------\n      ///////*767////////////////         OS: lilRISC 5.5 RV32I\n    //////7676767676*//////////////       Host: PYNQ Z2\n   /////76767//7676767//////////////      Kernel: haha, yea\n  /////767676///*76767///////////////     Uptime: since the beginning of time\n ///////767676///76767.///7676*///////    Packages: everything you need (0)\n/////////767676//76767///767676////////   Shell: Bowser's\n//////////76767676767////76767/////////   Resolution: graphics? where?\n///////////76767676//////7676//////////   Theme: PYNQ\n////////////,7676,///////767///////////   Icons: Beyonc\303\251\n/////////////*7676///////76////////////   Terminal: arnold\n///////////////7676////////////////////   CPU: The Alicia 2000 (70MHz)\n ///////////////7676///767////////////    GPU: future work\n  //////////////////////'////////////     Memory: let's not bodyshame Tux here\n   //////.7676767676767676767,//////\n    /////767676767676767676767/////\n      ///////////////////////////\n         /////////////////////\n             /////////////\n"