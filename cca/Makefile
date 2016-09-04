DEBUG=yes

#DEBUG
ifeq ($(DEBUG),yes)
	TAG=debug
	OPTS+= -g -O0 
else
	OPTS+= -O3
	TAG=release
endif


############################################
#Directorios
############################################
BUILD = build/$(TAG)
BIN   = bin/$(TAG)


OBJS= rtpsession.o rtp.o  stunmessage.o remoteratecontrol.o crc32calc.o fecdecoder.o
OBJSSENDER = $(OBJS) sender.o
OBJSRECEIVER = $(OBJS) receiver.o
BUILDOBJSSENDER = $(addprefix $(BUILD)/,$(OBJSSENDER))
BUILDOBJSRECEIVER = $(addprefix $(BUILD)/,$(OBJSRECEIVER))

###################################
#Compilacion condicional
###################################
VPATH  =  %.o $(BUILD)/

LDFLAGS+= -lpthread -lssl -lcrypto -lsrtp

CFLAGS  += $(OPTS)
CXXFLAGS+= $(OPTS)

%.o: %.c
	$(CC) $(CFLAGS) -c $< 		-o $(BUILD)/$@

%.o: %.cpp
	$(CXX) $(CXXFLAGS) -c $< 	-o $(BUILD)/$@


############################################
#Targets
############################################
all: touch mkdirs sender receiver

touch:
	touch version.h
mkdirs:
	mkdir -p $(BUILD)
	mkdir -p $(BIN)
clean:
	rm -f $(BUILDOBJSSENDER)
	rm -f $(BUILDOBJSRECEIVER)
	rm -f "$(BIN)/sender"
	rm -f "$(BIN)/receiver"


############################################
#Binaries
############################################

sender: $(OBJSSENDER)
	$(CXX) -o $(BIN)/$@ $(BUILDOBJSSENDER) $(LDFLAGS) 

receiver: $(OBJSRECEIVER)
	$(CXX) -o $(BIN)/$@ $(BUILDOBJSRECEIVER) $(LDFLAGS) 

