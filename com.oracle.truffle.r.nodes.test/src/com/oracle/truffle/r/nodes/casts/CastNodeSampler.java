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
package com.oracle.truffle.r.nodes.casts;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import com.oracle.truffle.r.nodes.unary.CastNode;

public class CastNodeSampler<T extends CastNode> {

    protected final T castNode;

    public CastNodeSampler(T castNode) {
        this.castNode = castNode;
    }

    public T getCastNode() {
        return castNode;
    }

    public final TypeExpr resultTypes() {
        return resultTypes(TypeExpr.ANYTHING);
    }

    public TypeExpr resultTypes(TypeExpr inputType) {
        return CastUtils.Casts.createCastNodeCasts(getClass()).narrow(inputType);
    }

    public final Samples<?> collectSamples() {
        Set<Type> resTypes = resultTypes().normalize().stream().filter(cls -> cls != Object.class).collect(Collectors.toSet());

        Set<?> defaultPositiveSamples;
        if (resTypes.isEmpty()) {
            defaultPositiveSamples = CastUtils.sampleValuesForType(Object.class);
        } else {
            defaultPositiveSamples = resTypes.stream().flatMap(cls -> CastUtils.sampleValuesForType(cls).stream()).collect(Collectors.toSet());
        }

        return collectSamples(TypeExpr.ANYTHING, new Samples<>(defaultPositiveSamples, Collections.emptySet()));
    }

    @SuppressWarnings("unused")
    public Samples<?> collectSamples(TypeExpr inputType, Samples<?> downStreamSamples) {
        return downStreamSamples;
    }

    public static <T extends CastNode> CastNodeSampler<T> createSampler(T castNode) {
        return createSampler(castNode, true);
    }

    @SuppressWarnings("unchecked")
    public static <T extends CastNode> CastNodeSampler<T> createSampler(T castNode, boolean useDefaultSampler) {
        if (castNode == null) {
            return null;
        }

        Class<? extends CastNode> castNodeCls = castNode.getClass();
        String clsName = castNodeCls.getName();
        String analyzerClsName = clsName + "Sampler";
        Class<?> analyzerCls;
        try {
            analyzerCls = Class.forName(analyzerClsName);
        } catch (ClassNotFoundException e) {
            if (useDefaultSampler) {
                return new CastNodeSampler<>(castNode);
            } else {
                throw new IllegalArgumentException("No sampler class found for cast node " + clsName);
            }
        }

        try {
            Constructor<?> analyzerConstr = analyzerCls.getConstructor(castNodeCls);
            return (CastNodeSampler<T>) analyzerConstr.newInstance(castNode);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
