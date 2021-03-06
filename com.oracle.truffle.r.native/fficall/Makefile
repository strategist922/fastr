#
# Copyright (c) 2014, 2016, Oracle and/or its affiliates. All rights reserved.
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

# Builds libR

ifneq (,$(wildcard $(TOPDIR)/platform.mk))
include $(TOPDIR)/platform.mk
else
ifneq ($(MAKECMDGOALS),clean)
$(error no platform.mk available)
endif
endif

.PHONY: all clean objs

C_LIBNAME := libR$(DYLIB_EXT)
C_LIB := $(FASTR_LIB_DIR)/$(C_LIBNAME)

ifeq ($(OS_NAME), Darwin)
VERSION_FLAGS := -current_version $(R_VERSION) -compatibility_version $(R_VERSION)
endif

BLAS_TARGET := $(FASTR_LIB_DIR)/libRblas$(DYLIB_EXT)

all: $(C_LIB)

$(C_LIB): objs 
	$(DYLIB_LD) $(DYLIB_LDFLAGS) -o $(C_LIB) $(wildcard lib/*.o) -L $(GNUR_HOME)/lib -lRblas $(VERSION_FLAGS)
ifeq ($(OS_NAME),Darwin)
	install_name_tool -change libRblas.dylib $(BLAS_TARGET) $(C_LIB)
endif

objs:
	$(MAKE) -C src/common all
	$(MAKE) -C src/jni all

clean:
	$(MAKE) -C src/common clean
	$(MAKE) -C src/jni clean
	rm -rf $(C_LIB)
	
