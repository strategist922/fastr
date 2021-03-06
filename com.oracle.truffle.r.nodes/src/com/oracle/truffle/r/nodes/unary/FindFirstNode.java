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
package com.oracle.truffle.r.nodes.unary;

import java.io.PrintWriter;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.r.nodes.binary.BoxPrimitiveNode;
import com.oracle.truffle.r.nodes.binary.BoxPrimitiveNodeGen;
import com.oracle.truffle.r.runtime.RError;
import com.oracle.truffle.r.runtime.data.RMissing;
import com.oracle.truffle.r.runtime.data.RNull;
import com.oracle.truffle.r.runtime.data.model.RAbstractVector;

public abstract class FindFirstNode extends CastNode {

    @Child private BoxPrimitiveNode boxPrimitive = BoxPrimitiveNodeGen.create();
    private final Class<?> elementClass;
    private final RError.Message message;
    private final Object[] messageArgs;
    private final PrintWriter out;
    private final Object defaultValue;

    private final BranchProfile warningProfile = BranchProfile.create();

    protected FindFirstNode(Class<?> elementClass, RError.Message message, Object[] messageArgs, PrintWriter out, Object defaultValue) {
        this.elementClass = elementClass;
        this.defaultValue = defaultValue;
        this.message = message;
        this.messageArgs = messageArgs;
        this.out = out;
    }

    protected FindFirstNode(Class<?> elementClass, Object defaultValue) {
        this(elementClass, null, null, null, defaultValue);
    }

    public Class<?> getElementClass() {
        return elementClass;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    @Specialization
    protected Object onNull(RNull x) {
        return handleMissingElement(x);
    }

    @Specialization
    protected Object onMissing(RMissing x) {
        return handleMissingElement(x);
    }

    @Specialization(guards = "isVectorEmpty(x)")
    protected Object onEmptyVector(RAbstractVector x) {
        return handleMissingElement(x);
    }

    @Specialization(guards = "!isVector(x)")
    protected Object onNonVector(Object x) {
        return x;
    }

    private Object handleMissingElement(Object x) {
        if (defaultValue != null) {
            if (message != null) {
                warningProfile.enter();
                handleArgumentWarning(x, this, message, messageArgs, out);
            }
            return defaultValue;
        } else {
            handleArgumentError(x, this, message, messageArgs);
            return null;
        }
    }

    @Specialization(guards = "!isVectorEmpty(x)")
    protected Object onVector(RAbstractVector x) {
        return x.getDataAtAsObject(0);
    }

    protected boolean isVectorEmpty(RAbstractVector x) {
        return x.getLength() == 0;
    }

    protected boolean isVector(Object x) {
        return x instanceof RAbstractVector;
    }

}
