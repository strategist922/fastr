/*
 * This material is distributed under the GNU General Public License
 * Version 2. You may review the terms of this license at
 * http://www.gnu.org/licenses/gpl-2.0.html
 *
 * Copyright (c) 2014, Purdue University
 * Copyright (c) 2014, 2016, Oracle and/or its affiliates
 *
 * All rights reserved.
 */
package com.oracle.truffle.r.nodes.builtin.base;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.r.nodes.builtin.RBuiltinNode;
import com.oracle.truffle.r.runtime.RBuiltin;
import com.oracle.truffle.r.runtime.RBuiltinKind;
import com.oracle.truffle.r.runtime.RDispatch;
import com.oracle.truffle.r.runtime.RRuntime;
import com.oracle.truffle.r.runtime.data.RArgsValuesAndNames;
import com.oracle.truffle.r.runtime.data.RComplex;
import com.oracle.truffle.r.runtime.data.model.RAbstractComplexVector;
import com.oracle.truffle.r.runtime.data.model.RAbstractDoubleVector;
import com.oracle.truffle.r.runtime.data.model.RAbstractIntVector;
import com.oracle.truffle.r.runtime.data.model.RAbstractLogicalVector;
import com.oracle.truffle.r.runtime.ops.BinaryArithmetic;

@RBuiltin(name = "prod", kind = RBuiltinKind.PRIMITIVE, parameterNames = {"...", "na.rm"}, dispatch = RDispatch.SUMMARY_GROUP_GENERIC)
public abstract class Prod extends RBuiltinNode {

    @Override
    public Object[] getDefaultParameterValues() {
        return new Object[]{RArgsValuesAndNames.EMPTY, RRuntime.LOGICAL_FALSE};
    }

    @Child private Prod prodRecursive;

    public abstract Object executeObject(Object x);

    @Child private BinaryArithmetic prod = BinaryArithmetic.MULTIPLY.create();

    @Specialization
    protected Object prod(RArgsValuesAndNames args) {
        if (prodRecursive == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            prodRecursive = insert(ProdNodeGen.create(null));
        }
        // TODO: eventually handle multiple vectors properly
        return prodRecursive.executeObject(args.getArgument(0));
    }

    @Specialization
    protected double prod(RAbstractDoubleVector x) {
        double product = x.getDataAt(0);
        for (int k = 1; k < x.getLength(); k++) {
            product = prod.op(product, x.getDataAt(k));
        }
        return product;
    }

    @Specialization
    protected double prod(RAbstractIntVector x) {
        double product = x.getDataAt(0);
        for (int k = 1; k < x.getLength(); k++) {
            product = prod.op(product, x.getDataAt(k));
        }
        return product;
    }

    @Specialization
    protected double prod(RAbstractLogicalVector x) {
        double product = x.getDataAt(0);
        for (int k = 1; k < x.getLength(); k++) {
            product = prod.op(product, x.getDataAt(k));
        }
        return product;
    }

    @Specialization
    protected RComplex prod(RAbstractComplexVector x) {
        RComplex product = x.getDataAt(0);
        for (int k = 1; k < x.getLength(); k++) {
            RComplex a = x.getDataAt(k);
            product = prod.op(product.getRealPart(), product.getImaginaryPart(), a.getRealPart(), a.getImaginaryPart());
        }
        return product;
    }
}
