#
# Copyright (c) 2014, 2015, Oracle and/or its affiliates. All rights reserved.
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
#
# This code is free software; you can redistribute it and/or modify it
# under the terms of the GNU General Public License version 2 only, as
# published by the Free Software Foundation.
#
# This code is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
# FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
# version 2 for more details (a copy is included in the LICENSE file that
# accompanied this code).
#
# You should have received a copy of the GNU General Public License version
# 2 along with this work; if not, write to the Free Software Foundation,
# Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
#
# Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
# or visit www.oracle.com if you need additional information or have any
# questions.
#

ifneq ($(MAKECMDGOALS),clean)
include $(TOPDIR)/platform.mk
endif

.PHONY: all clean

# location of compiled code (.o files)
OBJ = ../../lib

C_HDRS := $(wildcard *.h)

C_SOURCES = $(wildcard *.c)
C_OBJECTS := $(patsubst %.c,$(OBJ)/%.o,$(C_SOURCES))
#$(info C_OBJECTS=$(C_OBJECTS))

JNI_INCLUDES = -I $(JAVA_HOME)/include -I $(JAVA_HOME)/include/$(JDK_OS_DIR)
FFI_INCLUDES = -I$(TOPDIR)/include -I$(TOPDIR)/include/R_ext
LOCAL_INCLUDES = -I . -I $(abspath ../include)

INCLUDES := $(LOCAL_INCLUDES) $(JNI_INCLUDES) $(FFI_INCLUDES)

# uncomment to see exactly where headers are being read from
#CFLAGS := $(CFLAGS) -H

all: Makefile $(C_OBJECTS) 

$(C_OBJECTS): | $(OBJ)

$(OBJ):
	mkdir -p $(OBJ)

$(OBJ)/%.o: %.c $(TOPDIR)/include/Rinternals.h $(C_HDRS)
	$(CC) $(CFLAGS) $(INCLUDES) -I../variable_defs -c $< -o $@

# for debugging, to see what's really being compiled
$(OBJ)/%.E: %.c $(TOPDIR)/include/Rinternals.h
	$(CC) -E $(CFLAGS) $(INCLUDES) -c $< > $@

clean:
	rm -rf $(OBJ)
