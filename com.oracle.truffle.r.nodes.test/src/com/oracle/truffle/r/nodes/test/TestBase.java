/*
 * Copyright (c) 2014, 2015, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.truffle.r.nodes.test;

import org.junit.*;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.r.engine.*;
import com.oracle.truffle.r.options.*;
import com.oracle.truffle.r.runtime.*;
import com.oracle.truffle.r.runtime.data.*;
import com.oracle.truffle.r.runtime.ffi.*;

public class TestBase {

    @BeforeClass
    public static void setupClass() {
        Load_RFFIFactory.initialize();
        FastROptions.initialize();
        REnvVars.initialize();
        ROptions.initialize();
        ROptions.setValue("defaultPackages", RDataFactory.createStringVector(new String[]{}, true));
        REngine.initialize(new String[0], new ConsoleHandler(), false, true);
    }

    private static class ConsoleHandler implements RContext.ConsoleHandler {
        private final StringBuilder buffer = new StringBuilder();

        @TruffleBoundary
        public void println(String s) {
            buffer.append(s);
            buffer.append('\n');
        }

        @TruffleBoundary
        public void print(String s) {
            buffer.append(s);
        }

        @TruffleBoundary
        public void printf(String format, Object... args) {
            buffer.append(String.format(format, args));
        }

        public String readLine() {
            return null;
        }

        public boolean isInteractive() {
            return false;
        }

        @TruffleBoundary
        public void printErrorln(String s) {
            println(s);
        }

        @TruffleBoundary
        public void printError(String s) {
            print(s);
        }

        public void redirectError() {
            // always
        }

        public String getPrompt() {
            return null;
        }

        public void setPrompt(String prompt) {
            // ignore
        }

        @TruffleBoundary
        void reset() {
            buffer.delete(0, buffer.length());
        }

        public int getWidth() {
            return RContext.CONSOLE_WIDTH;
        }

    }

}