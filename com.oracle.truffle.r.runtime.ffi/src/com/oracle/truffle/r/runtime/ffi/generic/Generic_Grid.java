/*
 * Copyright (c) 2015, 2016, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.oracle.truffle.r.runtime.ffi.generic;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.r.runtime.env.REnvironment;
import com.oracle.truffle.r.runtime.ffi.DLL;
import com.oracle.truffle.r.runtime.ffi.GridRFFI;
import com.oracle.truffle.r.runtime.ffi.LibPaths;
import com.oracle.truffle.r.runtime.ffi.RFFIFactory;

public class Generic_Grid implements GridRFFI {
    private static final class GridProvider {
        private static GridProvider grid;
        private static long initGrid;
        private static long killGrid;

        @TruffleBoundary
        private GridProvider() {
            System.load(LibPaths.getPackageLibPath("grid"));
            initGrid = DLL.findSymbol("L_initGrid", "grid", DLL.RegisteredNativeSymbol.any());
            killGrid = DLL.findSymbol("L_killGrid", "grid", DLL.RegisteredNativeSymbol.any());
            assert initGrid != DLL.SYMBOL_NOT_FOUND && killGrid != DLL.SYMBOL_NOT_FOUND;
        }

        static GridProvider gridProvider() {
            if (grid == null) {
                grid = new GridProvider();
            }
            return grid;
        }

        @SuppressWarnings("static-method")
        long getInitGrid() {
            return initGrid;
        }

        @SuppressWarnings("static-method")
        long getKillGrid() {
            return killGrid;
        }
    }

    @Override
    public Object initGrid(REnvironment gridEvalEnv) {
        long initGrid = GridProvider.gridProvider().getInitGrid();
        return RFFIFactory.getRFFI().getCallRFFI().invokeCall(initGrid, "L_initGrid", new Object[]{gridEvalEnv});
    }

    @Override
    public Object killGrid() {
        long killGrid = GridProvider.gridProvider().getKillGrid();
        return RFFIFactory.getRFFI().getCallRFFI().invokeCall(killGrid, "L_killGrid", new Object[0]);
    }
}
