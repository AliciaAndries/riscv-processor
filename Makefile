NAME = all
RECOURCES = /home/alicia/Documents/thesis/riscv-processor/src/test/resources

nonpipelined:
	riscv32-unknown-elf-as -o $(RECOURCES)/$(NAME).elf -c $(RECOURCES)/$(NAME).s
	~/Documents/thesis/elf2hex-1.0.1/elf2hex --bit-width 32 --input $(RECOURCES)/$(NAME).elf --output $(RECOURCES)/$(NAME).hex
	python3 $(RECOURCES)/hex_vecinit.py $(RECOURCES)/$(NAME).hex $(RECOURCES)/$(NAME)_hex.txt $(RECOURCES)/$(NAME)_bin.txt
	sbt "runMain core.CoreNonPipelined"
	python3 $(RECOURCES)/init_mem.py $(RECOURCES)/$(NAME).hex

pipelined:
	riscv32-unknown-elf-as -o $(RECOURCES)/$(NAME).elf -c $(RECOURCES)/$(NAME).s
	~/Documents/thesis/elf2hex-1.0.1/elf2hex --bit-width 32 --input $(RECOURCES)/$(NAME).elf --output $(RECOURCES)/$(NAME).hex
	python3 $(RECOURCES)/hex_vecinit.py $(RECOURCES)/$(NAME).hex $(RECOURCES)/$(NAME)_hex.txt $(RECOURCES)/$(NAME)_bin.txt
	sbt "runMain core.CorePipelined"
	python3 $(RECOURCES)/init_mem.py $(RECOURCES)/$(NAME).hex

onlyfile:
	riscv32-unknown-elf-as -o $(RECOURCES)/$(NAME).elf -c $(RECOURCES)/$(NAME).s
	~/Documents/thesis/elf2hex-1.0.1/elf2hex --bit-width 32 --input $(RECOURCES)/$(NAME).elf --output $(RECOURCES)/$(NAME).hex
	python3 $(RECOURCES)/hex_vecinit.py $(RECOURCES)/$(NAME).hex $(RECOURCES)/$(NAME)_hex.txt $(RECOURCES)/$(NAME)_bin.txt