/*
 * Copyright (c) 2016, 2016, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.truffle.r.nodes.function;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.InstrumentableFactory;
import com.oracle.truffle.api.instrumentation.ProbeNode;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeCost;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.r.runtime.nodes.RSyntaxNode;

public class RCallBaseNodeWrapperFactory implements InstrumentableFactory<RCallBaseNode> {

    @NodeInfo(cost = NodeCost.NONE)
    public static final class RCallBaseNodeWrapper extends RCallBaseNode implements InstrumentableFactory.WrapperNode {
        @Child private RCallBaseNode delegate;
        @Child private ProbeNode probeNode;

        public RCallBaseNodeWrapper(RCallBaseNode delegate, ProbeNode probeNode) {
            assert delegate != null;
            this.delegate = delegate;
            this.probeNode = probeNode;
        }

        @Override
        public Node getDelegateNode() {
            return delegate;
        }

        @Override
        public ProbeNode getProbeNode() {
            return probeNode;
        }

        @Override
        public Object execute(VirtualFrame frame) {
            try {
                probeNode.onEnter(frame);
                Object returnValue = delegate.execute(frame);
                probeNode.onReturnValue(frame, returnValue);
                return returnValue;
            } catch (Throwable t) {
                probeNode.onReturnExceptional(frame, t);
                throw t;
            }
        }

        @Override
        public Object execute(VirtualFrame frame, Object function) {
            try {
                probeNode.onEnter(frame);
                Object returnValue = delegate.execute(frame, function);
                probeNode.onReturnValue(frame, returnValue);
                return returnValue;
            } catch (Throwable t) {
                probeNode.onReturnExceptional(frame, t);
                throw t;
            }
        }

        @Override
        public RSyntaxNode getRSyntaxNode() {
            return delegate.asRSyntaxNode();
        }
    }

    @Override
    public com.oracle.truffle.api.instrumentation.InstrumentableFactory.WrapperNode createWrapper(RCallBaseNode node, ProbeNode probe) {
        return new RCallBaseNodeWrapper(node, probe);
    }
}
