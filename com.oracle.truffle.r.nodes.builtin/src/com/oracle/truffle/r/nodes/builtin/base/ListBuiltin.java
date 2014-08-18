/*
 * Copyright (c) 2013, 2014, Oracle and/or its affiliates. All rights reserved.
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

import static com.oracle.truffle.r.runtime.RBuiltinKind.*;

import com.oracle.truffle.api.dsl.*;
import com.oracle.truffle.r.nodes.builtin.*;
import com.oracle.truffle.r.runtime.*;
import com.oracle.truffle.r.runtime.data.*;
import com.oracle.truffle.r.runtime.data.model.*;

@RBuiltin(name = "list", kind = PRIMITIVE, parameterNames = {"..."})
// TODO Is it really worth having all the individual specializations given that we have to have one
// for *every* type
// and the code is essentially equivalent for each one?
public abstract class ListBuiltin extends RBuiltinNode {

    @Specialization
    public RList list(@SuppressWarnings("unused") RMissing missing) {
        controlVisibility();
        return list(new Object[0]);
    }

    @Specialization
    public RList list(byte value) {
        controlVisibility();
        return list(new Object[]{value});
    }

    @Specialization
    public RList list(int value) {
        controlVisibility();
        return list(new Object[]{value});
    }

    @Specialization
    public RList list(double value) {
        controlVisibility();
        return list(new Object[]{value});
    }

    @Specialization
    public RList list(RRaw value) {
        controlVisibility();
        return list(new Object[]{value});
    }

    @Specialization
    public RList list(RComplex value) {
        controlVisibility();
        return list(new Object[]{value});
    }

    @Specialization
    public RList list(String value) {
        controlVisibility();
        return list(new Object[]{value});
    }

    @Specialization
    public RList list(RAbstractVector value) {
        controlVisibility();
        return list(new Object[]{value});
    }

    @Specialization
    public RList list(Object[] args) {
        controlVisibility();
        // TODO: should we duplicate all specializations for no-args and args case?
        return RDataFactory.createList(args, argNameVector());
    }

    @Specialization
    public RList list(RNull value) {
        controlVisibility();
        return RDataFactory.createList(new Object[]{value}, argNameVector());
    }

    @Specialization
    public RList list(REnvironment value) {
        controlVisibility();
        return RDataFactory.createList(new Object[]{value}, argNameVector());
    }

    @Specialization
    public RList list(RFunction value) {
        controlVisibility();
        return RDataFactory.createList(new Object[]{value}, argNameVector());
    }

    private RStringVector argNameVector() {
        String[] argNames = getSuppliedArgsNames();
        if (argNames == null) {
            return null;
        }
        String[] names = new String[argNames.length];
        for (int i = 0; i < names.length; i++) {
            String orgName = argNames[i];
            names[i] = (orgName == null ? RRuntime.NAMES_ATTR_EMPTY_VALUE : orgName);
        }
        return RDataFactory.createStringVector(names, RDataFactory.COMPLETE_VECTOR);
    }

}