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
import com.oracle.truffle.r.nodes.*;
import com.oracle.truffle.r.nodes.access.*;
import com.oracle.truffle.r.nodes.builtin.*;
import com.oracle.truffle.r.nodes.unary.*;
import com.oracle.truffle.r.runtime.*;
import com.oracle.truffle.r.runtime.data.*;

@RBuiltin(name = "complex", kind = SUBSTITUTE, parameterNames = {"length.out", "real", "imaginary", "modulus", "argument"})
// TODO INTERNAL
public abstract class Complex extends RBuiltinNode {

    @Override
    public RNode[] getParameterValues() {
        // FIXME have numeric vectors for real and imaginary
        return new RNode[]{ConstantNode.create(0), ConstantNode.create(0.0), ConstantNode.create(0.0), ConstantNode.create(1), ConstantNode.create(0)};
    }

    @CreateCast("arguments")
    protected RNode[] castStatusArgument(RNode[] arguments) {
        // length.out argument is at index 0
        arguments[0] = CastIntegerNodeFactory.create(arguments[0], true, false, false);
        return arguments;
    }

    @Specialization(guards = "zeroLength")
    @SuppressWarnings("unused")
    public RComplex complexZeroLength(int lengthOut, double real, double imaginary, int modulus, int argument) {
        controlVisibility();
        return RDataFactory.createComplex(real, imaginary);
    }

    @Specialization(guards = "!zeroLength")
    @SuppressWarnings("unused")
    public RComplexVector complex(int lengthOut, double real, double imaginary, int modulus, int argument) {
        controlVisibility();
        double[] data = new double[lengthOut << 1];
        for (int i = 0; i < data.length; i += 2) {
            data[i] = real;
            data[i + 1] = imaginary;
        }
        return RDataFactory.createComplexVector(data, !RRuntime.isNA(real) && !RRuntime.isNA(imaginary));
    }

    @SuppressWarnings("unused")
    protected static boolean zeroLength(int lengthOut, double real, double imaginary, int modulus, int argument) {
        return lengthOut == 0;
    }

}