/*
 * Copyright (c) 2013, 2016, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.truffle.r.nodes.builtin.base;

import static com.oracle.truffle.r.runtime.RBuiltinKind.INTERNAL;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.r.nodes.builtin.CastBuilder;
import com.oracle.truffle.r.nodes.builtin.RBuiltinNode;
import com.oracle.truffle.r.nodes.builtin.helpers.BrowserInteractNode;
import com.oracle.truffle.r.runtime.RBuiltin;
import com.oracle.truffle.r.runtime.RCmdOptions.RCmdOption;
import com.oracle.truffle.r.runtime.RError;
import com.oracle.truffle.r.runtime.RVisibility;
import com.oracle.truffle.r.runtime.Utils;
import com.oracle.truffle.r.runtime.context.ConsoleHandler;
import com.oracle.truffle.r.runtime.context.RContext;
import com.oracle.truffle.r.runtime.data.RNull;
import com.oracle.truffle.r.runtime.data.model.RAbstractStringVector;

@RBuiltin(name = "quit", visibility = RVisibility.OFF, kind = INTERNAL, parameterNames = {"save", "status", "runLast"})
public abstract class Quit extends RBuiltinNode {

    private static final String[] SAVE_VALUES = new String[]{"yes", "no", "ask", "default"};

    @Override
    protected void createCasts(CastBuilder casts) {
        casts.toInteger(1);
    }

    private void checkSaveValue(String save) throws RError {
        for (String saveValue : SAVE_VALUES) {
            if (saveValue.equals(save)) {
                return;
            }
        }
        throw RError.error(this, RError.Message.QUIT_SAVE);
    }

    @Specialization
    @TruffleBoundary
    protected Object doQuit(RAbstractStringVector saveArg, int status, byte runLast) {
        if (BrowserInteractNode.inBrowser()) {
            RError.warning(this, RError.Message.BROWSER_QUIT);
            return null;
        }
        String save = saveArg.getDataAt(0);
        checkSaveValue(save);
        // Quit does not divert its output to sink
        ConsoleHandler consoleHandler = RContext.getInstance().getConsoleHandler();
        if (save.equals("default")) {
            if (RContext.getInstance().getOptions().getBoolean(RCmdOption.NO_SAVE)) {
                save = "no";
            } else {
                if (consoleHandler.isInteractive()) {
                    save = "ask";
                } else {
                    // TODO options must be set, check
                }
            }
        }
        boolean doSave = false;
        if (save.equals("ask")) {
            W: while (true) {
                consoleHandler.setPrompt("");
                consoleHandler.print("Save workspace image? [y/n/c]: ");
                String response = consoleHandler.readLine();
                if (response == null) {
                    throw Utils.exit(status);
                }
                if (response.length() == 0) {
                    continue;
                }
                switch (response.charAt(0)) {
                    case 'c':
                        consoleHandler.setPrompt("> ");
                        return RNull.instance;
                    case 'y':
                        doSave = true;
                        break W;
                    case 'n':
                        doSave = false;
                        break W;
                    default:
                        continue;
                }
            }
        }

        if (doSave) {
            /*
             * we do not have an efficient way to tell if the global environment is "dirty", so we
             * save always
             */
            RContext.getEngine().checkAndRunStartupShutdownFunction("sys.save.image", new String[]{"\".RData\""});
            RContext.getInstance().getConsoleHandler().flushHistory();
        }
        if (runLast != 0) {
            RContext.getEngine().checkAndRunStartupShutdownFunction(".Last");
            // TODO errors should return to prompt if interactive
            RContext.getEngine().checkAndRunStartupShutdownFunction(".Last.sys");
        }
        // destroy the context inside exit() method as it still needs to access it
        Utils.exit(status);
        return null;
    }

    @SuppressWarnings("unused")
    @Fallback
    protected Object doQuit(Object saveArg, Object status, Object runLast) {
        throw RError.error(this, RError.Message.INVALID_OR_UNIMPLEMENTED_ARGUMENTS);
    }

}
