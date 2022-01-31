NAME = neofetch_v2
RECOURCES = /home/alicia/Documents/thesis/riscv-processor/src/test/resources

nonpipelined:
	riscv64-unknown-elf-as -march=rv32i -mabi=ilp32 -o $(RECOURCES)/$(NAME).elf -c $(RECOURCES)/$(NAME).s
	~/Documents/thesis/elf2hex-1.0.1/elf2hex --bit-width 32 --input $(RECOURCES)/$(NAME).elf --output $(RECOURCES)/$(NAME).hex
	python3 $(RECOURCES)/hex_vecinit.py $(RECOURCES)/$(NAME).hex $(RECOURCES)/$(NAME)_hex.txt $(RECOURCES)/$(NAME)_bin.txt
	sbt "runMain core.CoreNonPipelined"
	python3 $(RECOURCES)/init_mem.py $(RECOURCES)/$(NAME).hex

pipelined:
	riscv64-unknown-elf-as -march=rv32i -mabi=ilp32 -o $(RECOURCES)/$(NAME).elf -c $(RECOURCES)/$(NAME).s
	~/Documents/thesis/elf2hex-1.0.1/elf2hex --bit-width 32 --input $(RECOURCES)/$(NAME).elf --output $(RECOURCES)/$(NAME).hex
	python3 $(RECOURCES)/hex_vecinit.py $(RECOURCES)/$(NAME).hex $(RECOURCES)/$(NAME)_hex.txt $(RECOURCES)/$(NAME)_bin.txt
	sbt "runMain core.CorePipelined"
	python3 $(RECOURCES)/init_mem.py $(RECOURCES)/$(NAME).hex

pipelined_alu_split: 
	riscv64-unknown-elf-as -march=rv32i -mabi=ilp32 -o $(RECOURCES)/$(NAME).elf -c $(RECOURCES)/$(NAME).s
	~/Documents/thesis/elf2hex-1.0.1/elf2hex --bit-width 32 --input $(RECOURCES)/$(NAME).elf --output $(RECOURCES)/$(NAME).hex
	python3 $(RECOURCES)/hex_vecinit.py $(RECOURCES)/$(NAME).hex $(RECOURCES)/$(NAME)_hex.txt $(RECOURCES)/$(NAME)_bin.txt
	sbt "runMain core.CorePipelineALUSplit"
	python3 $(RECOURCES)/init_mem.py $(RECOURCES)/$(NAME).hex

pipelined_reg_only:
	riscv64-unknown-elf-as -march=rv32i -mabi=ilp32 -o $(RECOURCES)/$(NAME).elf -c $(RECOURCES)/$(NAME).s
	~/Documents/thesis/elf2hex-1.0.1/elf2hex --bit-width 32 --input $(RECOURCES)/$(NAME).elf --output $(RECOURCES)/$(NAME).hex
	python3 $(RECOURCES)/hex_vecinit.py $(RECOURCES)/$(NAME).hex $(RECOURCES)/$(NAME)_hex.txt $(RECOURCES)/$(NAME)_bin.txt
	sbt "runMain core.CorePipelinedRegOnly"
	python3 $(RECOURCES)/init_mem.py $(RECOURCES)/$(NAME).hex

pipelined_combined:
	riscv64-unknown-elf-as -march=rv32i -mabi=ilp32 -o $(RECOURCES)/$(NAME).elf -c $(RECOURCES)/$(NAME).s
	~/Documents/thesis/elf2hex-1.0.1/elf2hex --bit-width 32 --input $(RECOURCES)/$(NAME).elf --output $(RECOURCES)/$(NAME).hex
	python3 $(RECOURCES)/hex_vecinit.py $(RECOURCES)/$(NAME).hex $(RECOURCES)/$(NAME)_hex.txt $(RECOURCES)/$(NAME)_bin.txt
	sbt "runMain core.CoreCombined"
	python3 $(RECOURCES)/init_mem.py $(RECOURCES)/$(NAME).hex

onlyfile:
	riscv64-unknown-elf-as -march=rv32i -mabi=ilp32 -o $(RECOURCES)/$(NAME).elf -c $(RECOURCES)/$(NAME).s
	~/Documents/thesis/elf2hex-1.0.1/elf2hex --bit-width 32 --input $(RECOURCES)/$(NAME).elf --output $(RECOURCES)/$(NAME).hex
	python3 $(RECOURCES)/hex_vecinit.py $(RECOURCES)/$(NAME).hex $(RECOURCES)/$(NAME)_hex.txt $(RECOURCES)/$(NAME)_bin.txt