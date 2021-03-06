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
package com.oracle.truffle.r.nodes.control;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.ConditionProfile;
import com.oracle.truffle.api.source.SourceSection;
import com.oracle.truffle.r.nodes.unary.ConvertBooleanNode;
import com.oracle.truffle.r.runtime.ArgumentsSignature;
import com.oracle.truffle.r.runtime.RError;
import com.oracle.truffle.r.runtime.RRuntime;
import com.oracle.truffle.r.runtime.RSerialize;
import com.oracle.truffle.r.runtime.context.RContext;
import com.oracle.truffle.r.runtime.data.RNull;
import com.oracle.truffle.r.runtime.gnur.SEXPTYPE;
import com.oracle.truffle.r.runtime.nodes.RNode;
import com.oracle.truffle.r.runtime.nodes.RSourceSectionNode;
import com.oracle.truffle.r.runtime.nodes.RSyntaxCall;
import com.oracle.truffle.r.runtime.nodes.RSyntaxElement;
import com.oracle.truffle.r.runtime.nodes.RSyntaxLookup;
import com.oracle.truffle.r.runtime.nodes.RSyntaxNode;

public final class IfNode extends RSourceSectionNode implements RSyntaxNode, RSyntaxCall {

    @Child private ConvertBooleanNode condition;
    @Child private RNode thenPart;
    @Child private RNode elsePart;

    private final ConditionProfile conditionProfile = ConditionProfile.createCountingProfile();

    private IfNode(SourceSection src, RSyntaxNode condition, RSyntaxNode thenPart, RSyntaxNode elsePart) {
        super(src);
        this.condition = ConvertBooleanNode.create(condition);
        this.thenPart = thenPart.asRNode();
        this.elsePart = elsePart == null ? null : elsePart.asRNode();
    }

    public static IfNode create(SourceSection src, RSyntaxNode condition, RSyntaxNode thenPart, RSyntaxNode elsePart) {
        IfNode ifNode = new IfNode(src, condition, thenPart, elsePart == null ? null : elsePart);
        return ifNode;
    }

    /**
     * Result visibility of an {@code if} expression is not only a property of the {@code if}
     * builtin; it also depends on whether there is an else branch or not, and on the condition. For
     * instance, the expression {@code if (FALSE) 23} will evaluate to {@code NULL}, but the result
     * will not be printed in the shell. Conversely, {@code NULL} will be printed for
     * {@code if (FALSE) 23 else NULL} because the else branch is given.
     */

    @Override
    public Object execute(VirtualFrame frame) {
        byte cond = condition.executeByte(frame);
        RContext.getInstance().setVisible(elsePart != null || cond == RRuntime.LOGICAL_TRUE);

        if (cond == RRuntime.LOGICAL_NA) {
            // NA is the only remaining option
            CompilerDirectives.transferToInterpreter();
            throw RError.error(this, RError.Message.NA_UNEXP);
        }

        if (conditionProfile.profile(cond == RRuntime.LOGICAL_TRUE)) {
            return thenPart.execute(frame);
        } else {
            assert cond == RRuntime.LOGICAL_FALSE : "logical value none of TRUE|FALSE|NA";

            if (elsePart != null) {
                return elsePart.execute(frame);
            } else {
                return RNull.instance;
            }
        }
    }

    public ConvertBooleanNode getCondition() {
        return condition;
    }

    public RNode getThenPart() {
        return thenPart;
    }

    public RNode getElsePart() {
        return elsePart;
    }

    @Override
    public void serializeImpl(RSerialize.State state) {
        state.setAsBuiltin("if");
        state.openPairList(SEXPTYPE.LISTSXP);
        // condition
        state.serializeNodeSetCar(condition);
        // then, with brace
        state.openPairList(SEXPTYPE.LISTSXP);
        state.serializeNodeSetCar(thenPart);
        if (elsePart != null) {
            state.openPairList(SEXPTYPE.LISTSXP);
            state.serializeNodeSetCar(elsePart);
        }
        state.linkPairList(elsePart == null ? 2 : 3);
        state.setCdr(state.closePairList());
    }

    @Override
    public RSyntaxElement getSyntaxLHS() {
        return RSyntaxLookup.createDummyLookup(getSourceSection(), "if", true);
    }

    @Override
    public RSyntaxElement[] getSyntaxArguments() {
        if (elsePart == null) {
            return new RSyntaxElement[]{condition.asRSyntaxNode(), thenPart.asRSyntaxNode()};
        } else {
            return new RSyntaxElement[]{condition.asRSyntaxNode(), thenPart.asRSyntaxNode(), elsePart.asRSyntaxNode()};
        }
    }

    @Override
    public ArgumentsSignature getSyntaxSignature() {
        return ArgumentsSignature.empty(elsePart == null ? 2 : 3);
    }
}
