/*
 * Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.truffle.r.nodes.builtin;

import static com.oracle.truffle.r.nodes.builtin.CastBuilder.Predef.complexValue;
import static com.oracle.truffle.r.nodes.builtin.CastBuilder.Predef.defaultValue;
import static com.oracle.truffle.r.nodes.builtin.CastBuilder.Predef.doubleValue;
import static com.oracle.truffle.r.nodes.builtin.CastBuilder.Predef.gte;
import static com.oracle.truffle.r.nodes.builtin.CastBuilder.Predef.integerValue;
import static com.oracle.truffle.r.nodes.builtin.CastBuilder.Predef.logicalValue;
import static com.oracle.truffle.r.nodes.builtin.CastBuilder.Predef.lte;
import static com.oracle.truffle.r.nodes.builtin.CastBuilder.Predef.notEmpty;
import static com.oracle.truffle.r.nodes.builtin.CastBuilder.Predef.numericValue;
import static com.oracle.truffle.r.nodes.builtin.CastBuilder.Predef.scalarLogicalValue;
import static com.oracle.truffle.r.nodes.builtin.CastBuilder.Predef.singleElement;
import static com.oracle.truffle.r.nodes.builtin.CastBuilder.Predef.stringValue;
import static com.oracle.truffle.r.nodes.builtin.CastBuilder.Predef.toBoolean;
import static com.oracle.truffle.r.nodes.casts.CastUtils.samples;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.StringWriter;
import java.util.Arrays;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.r.nodes.access.AccessArgumentNode;
import com.oracle.truffle.r.nodes.builtin.ArgumentFilter.ArgumentTypeFilter;
import com.oracle.truffle.r.nodes.builtin.base.ColSums;
import com.oracle.truffle.r.nodes.builtin.base.ColSumsNodeGen;
import com.oracle.truffle.r.nodes.casts.ArgumentFilterSampler;
import com.oracle.truffle.r.nodes.casts.CastNodeSampler;
import com.oracle.truffle.r.nodes.casts.PredefFiltersSamplers;
import com.oracle.truffle.r.nodes.casts.PredefMappersSamplers;
import com.oracle.truffle.r.nodes.casts.Samples;
import com.oracle.truffle.r.nodes.casts.ValuePredicateArgumentFilterSampler;
import com.oracle.truffle.r.nodes.test.TestUtilities;
import com.oracle.truffle.r.nodes.test.TestUtilities.NodeHandle;
import com.oracle.truffle.r.nodes.unary.CastNode;
import com.oracle.truffle.r.runtime.ArgumentsSignature;
import com.oracle.truffle.r.runtime.RBuiltin;
import com.oracle.truffle.r.runtime.RError;
import com.oracle.truffle.r.runtime.RError.Message;
import com.oracle.truffle.r.runtime.RRuntime;
import com.oracle.truffle.r.runtime.data.RComplex;
import com.oracle.truffle.r.runtime.data.RDataFactory;
import com.oracle.truffle.r.runtime.data.RNull;
import com.oracle.truffle.r.runtime.data.model.RAbstractIntVector;
import com.oracle.truffle.r.runtime.data.model.RAbstractStringVector;
import com.oracle.truffle.r.runtime.nodes.RNode;

public class CastBuilderTest {

    private CastBuilder cb;
    private StringWriter out;

    @Before
    public void setUp() {
        CastBuilder.Predef.setPredefFilters(new PredefFiltersSamplers());
        CastBuilder.Predef.setPredefMappers(new PredefMappersSamplers());
        cb = new CastBuilder(null);
        out = new StringWriter();
        cb.output(out);
    }

    @After
    public void tearDown() {
        cb = null;
        out = null;
    }

    @Test
    public void testError() {
        cb.arg(0).mustBe(
                        ValuePredicateArgumentFilterSampler.omLambdaWithResTypes(x -> x instanceof String, String.class),
                        RError.Message.DLL_LOAD_ERROR, CastBuilder.ARG, "123");
        testPipeline();

        assertEquals("A", cast("A"));
        try {
            cast(Boolean.FALSE);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("unable to load shared object 'false'\n  123", e.getMessage());
        }

    }

    @Test
    public void testErrorWithAttachedPredicate() {
        cb.arg(0).mustBe(ValuePredicateArgumentFilterSampler.omLambdaWithResTypes(x -> x instanceof RAbstractIntVector || x instanceof Integer, Object.class), Message.SEED_NOT_VALID_INT);
        testPipeline();

        RAbstractIntVector v = RDataFactory.createIntVectorFromScalar(1);
        assertEquals(v, cast(v));
        try {
            cast(Boolean.FALSE);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals(RError.Message.SEED_NOT_VALID_INT.message, e.getMessage());
        }
    }

    @Test
    public void testWarning() {
        cb.arg(0).shouldBe(ValuePredicateArgumentFilterSampler.omLambdaWithResTypes(x -> x instanceof String, Object.class), RError.Message.DLL_LOAD_ERROR, CastBuilder.ARG, "123");
        testPipeline();

        assertEquals("A", cast("A"));
        assertEquals(Boolean.FALSE, cast(Boolean.FALSE));
        assertEquals("unable to load shared object 'false'\n  123", out.toString());
    }

    @Test
    public void testWarningWithAttachedPredicate() {
        cb.arg(0).shouldBe(integerValue(), Message.SEED_NOT_VALID_INT);
        testPipeline();

        RAbstractIntVector v = RDataFactory.createIntVectorFromScalar(1);
        assertEquals(v, cast(v));
        assertEquals(Boolean.FALSE, cast(Boolean.FALSE));
        assertEquals(RError.Message.SEED_NOT_VALID_INT.message, out.toString());
    }

    @Test
    public void testDefaultError() {
        cb.arg(0, "arg0").mustBe(ValuePredicateArgumentFilterSampler.fromLambdaWithSamples(x -> false, samples(), samples(true, false), Boolean.class));
        testPipeline(false);

        try {
            cast(Boolean.FALSE);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals(String.format(RError.Message.INVALID_ARGUMENT.message, "arg0"), e.getMessage());
        }
    }

    @Test
    public void testIsInteger() {
        cb.arg(0).mustBe(integerValue(), RError.Message.SEED_NOT_VALID_INT);
        testPipeline();

        RAbstractIntVector v = RDataFactory.createIntVectorFromScalar(1);
        assertEquals(v, cast(v));
        assertEquals(1, cast(1));
        try {
            cast("x");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals(RError.Message.SEED_NOT_VALID_INT.message, e.getMessage());
        }
    }

    @Test
    public void testIsNumericOrComplex() {
        cb.arg(0).mustBe(numericValue().or(complexValue()), RError.Message.SEED_NOT_VALID_INT);
        testPipeline();

        assertEquals(1, cast(1));
        assertEquals(RRuntime.LOGICAL_FALSE, cast(RRuntime.LOGICAL_FALSE));
        assertEquals(1.3d, cast(1.3d));
        RComplex c = RDataFactory.createComplex(1, 2);
        assertEquals(c, cast(c));

        Object v = RDataFactory.createIntVectorFromScalar(1);
        assertEquals(v, cast(v));
        v = RDataFactory.createLogicalVectorFromScalar(RRuntime.LOGICAL_FALSE);
        assertEquals(v, cast(v));
        v = RDataFactory.createDoubleVectorFromScalar(1.2);
        assertEquals(v, cast(v));
        v = RDataFactory.createComplexVectorFromScalar(c);
        assertEquals(v, cast(v));

        try {
            cast("x");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals(RError.Message.SEED_NOT_VALID_INT.message, e.getMessage());
        }
    }

    @Test
    public void testAsInteger() {
        cb.arg(0).asIntegerVector();
        testPipeline();

        assertEquals(1, cast(1));
        assertEquals(0, cast(RRuntime.LOGICAL_FALSE));
        assertEquals(1, cast(1.3d));
        RComplex c = RDataFactory.createComplex(1, 0);
        assertEquals(1, cast(c));
    }

    @Test
    public void testAsDouble() {
        cb.arg(0).asDoubleVector();
        testPipeline();

        assertEquals(1.2, cast(1.2));
    }

    @Test
    public void testAsLogical() {
        cb.arg(0).asLogicalVector();
        testPipeline();

        assertEquals(RRuntime.LOGICAL_TRUE, cast(1.2));
    }

    @Test
    public void testAsString() {
        cb.arg(0).asStringVector();
        testPipeline();

        assertEquals("1.2", cast(1.2));
    }

    @Test
    public void testEmptyError() {
        cb.arg(0, "x").asIntegerVector().mustBe(notEmpty());
        testPipeline();

        try {
            cast(RDataFactory.createIntVector(0));
        } catch (IllegalArgumentException e) {
            assertEquals(String.format(RError.Message.INVALID_ARGUMENT.message, "x"), e.getMessage());
        }
    }

    @Test
    public void testEmptyErrorWithCustomMessage() {
        cb.arg(0).asIntegerVector().mustBe(notEmpty(), RError.Message.SEED_NOT_VALID_INT);
        testPipeline();

        try {
            cast(RDataFactory.createIntVector(0));
        } catch (IllegalArgumentException e) {
            assertEquals(RError.Message.SEED_NOT_VALID_INT.message, e.getMessage());
        }
    }

    @Test
    public void testSizeWarning() {
        cb.arg(0).defaultWarning(RError.Message.LENGTH_GT_1).asIntegerVector().shouldBe(singleElement());
        testPipeline();

        cast(RDataFactory.createIntVector(new int[]{1, 2}, true));
        assertEquals(RError.Message.LENGTH_GT_1.message, out.toString());
    }

    @Test
    public void testSizeWarningWithCustomMessage() {
        cb.arg(0).asIntegerVector().shouldBe(singleElement(), RError.Message.SEED_NOT_VALID_INT);
        testPipeline();

        cast(RDataFactory.createIntVector(new int[]{1, 2}, true));
        assertEquals(RError.Message.SEED_NOT_VALID_INT.message, out.toString());
    }

    @Test
    public void testFindFirst() {
        cb.arg(0).asIntegerVector().findFirst(0);
        testPipeline();

        assertEquals(1, cast(1));
        assertEquals(1, cast(RDataFactory.createIntVector(new int[]{1, 2}, true)));
        assertEquals(1, cast("1"));
        assertEquals(0, cast(RDataFactory.createIntVector(0)));
    }

    @Test
    public void testFindFirstWithDefaultValue() {
        cb.arg(0).asIntegerVector().findFirst(-1);
        testPipeline();

        assertEquals(1, cast(1));
        assertEquals(1, cast(RDataFactory.createIntVector(new int[]{1, 2}, true)));
        assertEquals(1, cast("1"));
        assertEquals(-1, cast(RDataFactory.createIntVector(0)));
    }

    @Test
    public void testFindFirstThrow() {
        cb.arg(0).asIntegerVector().mustBe(notEmpty(), RError.Message.SEED_NOT_VALID_INT).findFirst();
        testPipeline();

        assertEquals(1, cast(1));
        assertEquals(1, cast(RDataFactory.createIntVector(new int[]{1, 2}, true)));
        assertEquals(1, cast("1"));
        try {
            cast(RDataFactory.createIntVector(0));
        } catch (IllegalArgumentException e) {
            assertEquals(RError.Message.SEED_NOT_VALID_INT.message, e.getMessage());
        }
    }

    @Test
    public void testNoLogicalNA() {
        cb.arg(0).asLogicalVector().findFirst().notNA(RRuntime.LOGICAL_TRUE);
        testPipeline();

        assertEquals(RRuntime.LOGICAL_TRUE, cast(RRuntime.LOGICAL_NA));
    }

    @Test
    public void testNoIntegerNA() {
        cb.arg(0).asIntegerVector().findFirst().notNA(1);
        testPipeline();

        assertEquals(1, cast(RRuntime.INT_NA));
    }

    @Test
    public void testNoDoubleNA() {
        cb.arg(0).asDoubleVector().findFirst().notNA(1.1);
        testPipeline();

        assertEquals(1.1, cast(RRuntime.DOUBLE_NA));
    }

    @Test
    public void testNoStringNA() {
        cb.arg(0).asStringVector().findFirst().notNA("A");
        testPipeline();

        assertEquals("A", cast(RRuntime.DOUBLE_NA));
    }

    @Test
    public void testSample0() {
        cb.arg(0, "x").asIntegerVector().shouldBe(singleElement()).findFirst(0);
        testPipeline();

        assertEquals(1, cast(RDataFactory.createIntVector(new int[]{1, 2}, true)));
        assertEquals(String.format(RError.Message.INVALID_ARGUMENT.message, "x"), out.toString());
    }

    @Test
    public void testSample1() {
        cb.arg(0).asIntegerVector().mustBe(notEmpty(), RError.Message.LENGTH_ZERO);
        testPipeline();

        try {
            cast(RDataFactory.createIntVector(0));
        } catch (IllegalArgumentException e) {
            assertEquals(RError.Message.LENGTH_ZERO.message, e.getMessage());
        }
    }

    @Test
    public void testSample2() {
        cb.arg(0).asIntegerVector().shouldBe(singleElement(), RError.Message.INVALID_USE, 1).mustBe(notEmpty(), RError.Message.ARGUMENT_EMPTY, 1);
        testPipeline();

        cast(RDataFactory.createIntVector(new int[]{1, 2}, true));
        assertEquals(String.format(RError.Message.INVALID_USE.message, 1), out.toString());

        try {
            cast(RDataFactory.createIntVector(0));
        } catch (IllegalArgumentException e) {
            assertEquals(String.format(RError.Message.ARGUMENT_EMPTY.message, 1), e.getMessage());
        }
    }

    @Test
    public void testSample3() {
        cb.arg(0).asLogicalVector().findFirst(RRuntime.LOGICAL_FALSE).map(toBoolean());
        testPipeline();

        assertEquals(Boolean.TRUE, cast(RDataFactory.createLogicalVector(new byte[]{RRuntime.LOGICAL_TRUE, RRuntime.LOGICAL_FALSE}, true)));
        assertEquals(Boolean.FALSE, cast(RDataFactory.createLogicalVector(0)));
    }

    @Test
    public void testSample4() {
        // the predicate is attached to the error message
        cb.arg(0).mustBe(ValuePredicateArgumentFilterSampler.omLambdaWithResTypes(x -> x instanceof RAbstractIntVector || x instanceof Integer, Object.class),
                        Message.SEED_NOT_VALID_INT).asIntegerVector();
        testPipeline();

        cast(RDataFactory.createIntVector(new int[]{1, 2}, true));

        try {
            cast("x");
        } catch (IllegalArgumentException e) {
            assertEquals(RError.Message.SEED_NOT_VALID_INT.message, e.getMessage());
        }
    }

    @Test
    public void testSample5() {
        ArgumentTypeFilter<Object, Object> complexOrExpr = integerValue().or(doubleValue()).or(complexValue()).or(logicalValue());
        Assert.assertTrue(complexOrExpr instanceof ArgumentFilterSampler);
        cb.arg(0).defaultError(RError.Message.INVALID_ARGUMENT, "fill").mustBe(numericValue().or(logicalValue())).asVector().mustBe(singleElement()).findFirst().shouldBe(
                        ValuePredicateArgumentFilterSampler.omLambdaWithResTypes(x -> x instanceof Byte || x instanceof Integer && ((Integer) x) > 0), Message.NON_POSITIVE_FILL).mapIf(
                                        scalarLogicalValue(), toBoolean());
        testPipeline();

        assertEquals(true, cast(RRuntime.LOGICAL_TRUE));
        assertEquals(10, cast(10));
        try {
            cast("xyz");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals(String.format(RError.Message.INVALID_ARGUMENT.message, "fill"), e.getMessage());
        }
        try {
            cast(RDataFactory.createIntVector(new int[]{1, 2}, true));
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals(String.format(RError.Message.INVALID_ARGUMENT.message, "fill"), e.getMessage());
        }
        try {
            cast(RDataFactory.createIntVector(0));
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals(String.format(RError.Message.INVALID_ARGUMENT.message, "fill"), e.getMessage());
        }
        cast(-10); // warning
        assertEquals(String.format(Message.NON_POSITIVE_FILL.message, "fill"), out.toString());
    }

    @Test
    public void testSample6() {
        cb.arg(0).map(defaultValue(RDataFactory.createStringVector(0))).mustBe(stringValue(), RError.Message.INVALID_ARGUMENT, "labels").asStringVector();
        testPipeline();

        assertEquals("abc", cast("abc"));

        Object asv = cast(RNull.instance);
        assertEquals(true, asv instanceof RAbstractStringVector);
        assertEquals(0, ((RAbstractStringVector) asv).getLength());

        RAbstractStringVector sv = RDataFactory.createStringVector(new String[]{"abc", "def"}, true);
        assertEquals(sv, cast(sv));

        sv = RDataFactory.createStringVector(0);
        asv = cast(sv);
        assertEquals(true, asv instanceof RAbstractStringVector);
        assertEquals(0, ((RAbstractStringVector) asv).getLength());

        try {
            cast(123);
        } catch (IllegalArgumentException e) {
            assertEquals(String.format(RError.Message.INVALID_ARGUMENT.message, "labels"), e.getMessage());
        }

        try {
            cast(RNull.instance);
        } catch (IllegalArgumentException e) {
            assertEquals(String.format(RError.Message.INVALID_ARGUMENT.message, "labels"), e.getMessage());
        }
    }

    @Test
    public void testSample7() {
        cb.arg(0, "justify").asIntegerVector().findFirst().mustBe(gte(2).and(lte(5)).not());

        testPipeline(true);

    }

    class RBuiltinRootNode extends RootNode {

        @Child private RBuiltinNode builtinNode;

        RBuiltinRootNode(RBuiltinNode builtinNode) {
            super(TruffleLanguage.class, null, null);
            this.builtinNode = builtinNode;
        }

        @Override
        public Object execute(VirtualFrame frame) {
            return builtinNode.execute(frame);
        }
    }

    @Test
    public void autoTestColSums() {
        RBuiltin annotation = ColSums.class.getAnnotation(RBuiltin.class);
        String[] parameterNames = annotation.parameterNames();
        parameterNames = Arrays.stream(parameterNames).map(n -> n.isEmpty() ? null : n).toArray(String[]::new);
        ArgumentsSignature signature = ArgumentsSignature.get(parameterNames);

        int total = signature.getLength();
        RNode[] args = new RNode[total];
        for (int i = 0; i < total; i++) {
            args[i] = AccessArgumentNode.create(i);
        }
        RBuiltinNode builtinNode = ColSumsNodeGen.create(args.clone());

        CastNode[] castNodes = builtinNode.getCasts();
        for (int i = 0; i < castNodes.length; i++) {
            CastNode castNode = builtinNode.getCasts()[i];
            if (castNode == null) {
                System.out.println("No Samples");
            } else {
                Samples<?> s = CastNodeSampler.createSampler(castNode).collectSamples();
                System.out.println("Samples:\n" + s);
            }
        }

        // RootCallTarget builtinNodeCallTarget = Truffle.getRuntime().createCallTarget(new
        // RBuiltinRootNode(builtinNode));
        // builtinNodeCallTarget.call(RArguments.createUnitialized(RDataFactory.createIntVector(new
        // int[]{1}, true), 1, 1, 1));
    }

    private Object cast(Object arg) {
        CastNode argCastNode = cb.getCasts()[0];
        NodeHandle<CastNode> argCastNodeHandle = TestUtilities.createHandle(argCastNode, (node, args) -> node.execute(args[0]));
        return argCastNodeHandle.call(arg);
    }

    private void testPipeline() {
        testPipeline(true);
    }

    private void testPipeline(boolean positiveMustNotBeEmpty) {
        CastNodeSampler<CastNode> sampler = CastNodeSampler.createSampler(cb.getCasts()[0]);
        System.out.println(sampler);
        // Samples<?> samples = sampler.collectSamples();
        //
        // if (positiveMustNotBeEmpty) {
        // Assert.assertFalse(samples.positiveSamples().isEmpty());
        // }
        //
        // testPipeline(samples);
    }

    private void testPipeline(Samples<?> samples) {

        for (Object sample : samples.positiveSamples()) {
            try {
                cast(sample);
            } catch (Exception e) {
                e.printStackTrace();
                fail();
            }
        }
        for (Object sample : samples.negativeSamples()) {
            try {
                cast(sample);
                fail();
            } catch (Exception e) {
                // ok
            }
        }
    }
}
