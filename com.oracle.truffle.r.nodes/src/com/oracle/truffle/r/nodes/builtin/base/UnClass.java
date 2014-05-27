/*
 * This material is distributed under the GNU General Public License
 * Version 2. You may review the terms of this license at
 * http://www.gnu.org/licenses/gpl-2.0.html
 *
 * Copyright (c) 2014, Purdue University
 * Copyright (c) 2014, Oracle and/or its affiliates
 *
 * All rights reserved.
 */

package com.oracle.truffle.r.nodes.builtin.base;

import static com.oracle.truffle.r.nodes.builtin.RBuiltinKind.PRIMITIVE;
import com.oracle.truffle.api.dsl.*;
import com.oracle.truffle.r.nodes.builtin.*;
import com.oracle.truffle.r.runtime.data.*;
import com.oracle.truffle.r.runtime.data.model.*;

@RBuiltin(name = "unclass", kind = PRIMITIVE)
public abstract class UnClass extends RBuiltinNode {

    @Specialization
    public Object unClass(RAbstractVector arg) {
        controlVisibility();
        if (arg.isObject()) {
            RVector resultVector = arg.materialize();
            if (resultVector.isShared()) {
                resultVector = resultVector.copy();
            }
            return RVector.setClassAttr(resultVector, null, null);
        }
        return arg;
    }

    @Specialization
    public Object unClass(RDataFrame arg) {
        controlVisibility();
        RDataFrame resultFrame = arg;
        if (resultFrame.isShared()) {
            resultFrame = resultFrame.copy();
        }
        return RVector.setClassAttr(resultFrame.getVector(), null, arg);
    }

}
