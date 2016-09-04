#
# Generated Makefile - do not edit!
#
# Edit the Makefile in the project folder instead (../Makefile). Each target
# has a -pre and a -post target defined where you can add customized code.
#
# This makefile implements configuration specific macros and targets.


# Environment
MKDIR=mkdir
CP=cp
GREP=grep
NM=nm
CCADMIN=CCadmin
RANLIB=ranlib
CC=gcc
CCC=g++
CXX=g++
FC=
AS=as

# Macros
CND_PLATFORM=GNU-Linux-x86
CND_CONF=Release
CND_DISTDIR=dist

# Include project Makefile
include Makefile

# Object Directory
OBJECTDIR=build/${CND_CONF}/${CND_PLATFORM}

# Object Files
OBJECTFILES= \
	${OBJECTDIR}/sender.o \
	${OBJECTDIR}/remoteratecontrol.o \
	${OBJECTDIR}/rtp.o \
	${OBJECTDIR}/receiver.o \
	${OBJECTDIR}/rtpsession.o \
	${OBJECTDIR}/crc32calc.o \
	${OBJECTDIR}/stunmessage.o \
	${OBJECTDIR}/fecdecoder.o


# C Compiler Flags
CFLAGS=

# CC Compiler Flags
CCFLAGS=
CXXFLAGS=

# Fortran Compiler Flags
FFLAGS=

# Assembler Flags
ASFLAGS=

# Link Libraries and Options
LDLIBSOPTIONS=

# Build Targets
.build-conf: ${BUILD_SUBPROJECTS}
	"${MAKE}"  -f nbproject/Makefile-Release.mk build/Release/GNU-Linux-x86/tests/TestFiles/f1

build/Release/GNU-Linux-x86/tests/TestFiles/f1: ${OBJECTFILES}
	${MKDIR} -p build/Release/GNU-Linux-x86/tests/TestFiles
	${LINK.cc} -o ${TESTDIR}/TestFiles/f1 ${OBJECTFILES} ${LDLIBSOPTIONS} 

${OBJECTDIR}/sender.o: sender.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} $@.d
	$(COMPILE.cc) -O2 -I. -MMD -MP -MF $@.d -o ${OBJECTDIR}/sender.o sender.cpp

${OBJECTDIR}/remoteratecontrol.o: remoteratecontrol.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} $@.d
	$(COMPILE.cc) -O2 -I. -MMD -MP -MF $@.d -o ${OBJECTDIR}/remoteratecontrol.o remoteratecontrol.cpp

${OBJECTDIR}/rtp.o: rtp.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} $@.d
	$(COMPILE.cc) -O2 -I. -MMD -MP -MF $@.d -o ${OBJECTDIR}/rtp.o rtp.cpp

${OBJECTDIR}/receiver.o: receiver.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} $@.d
	$(COMPILE.cc) -O2 -I. -MMD -MP -MF $@.d -o ${OBJECTDIR}/receiver.o receiver.cpp

${OBJECTDIR}/rtpsession.o: rtpsession.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} $@.d
	$(COMPILE.cc) -O2 -I. -MMD -MP -MF $@.d -o ${OBJECTDIR}/rtpsession.o rtpsession.cpp

${OBJECTDIR}/crc32calc.o: crc32calc.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} $@.d
	$(COMPILE.cc) -O2 -I. -MMD -MP -MF $@.d -o ${OBJECTDIR}/crc32calc.o crc32calc.cpp

${OBJECTDIR}/stunmessage.o: stunmessage.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} $@.d
	$(COMPILE.cc) -O2 -I. -MMD -MP -MF $@.d -o ${OBJECTDIR}/stunmessage.o stunmessage.cpp

${OBJECTDIR}/fecdecoder.o: fecdecoder.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} $@.d
	$(COMPILE.cc) -O2 -I. -MMD -MP -MF $@.d -o ${OBJECTDIR}/fecdecoder.o fecdecoder.cpp

# Subprojects
.build-subprojects:

# Clean Targets
.clean-conf: ${CLEAN_SUBPROJECTS}
	${RM} -r build/Release
	${RM} build/Release/GNU-Linux-x86/tests/TestFiles/f1

# Subprojects
.clean-subprojects:

# Enable dependency checking
.dep.inc: .depcheck-impl

include .dep.inc
