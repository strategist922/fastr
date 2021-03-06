/*
 * Copyright (c) 2014, 2016, Oracle and/or its affiliates. All rights reserved.
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

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.LoopConditionProfile;
import com.oracle.truffle.r.nodes.attributes.UnaryCopyAttributesNode;
import com.oracle.truffle.r.nodes.attributes.UnaryCopyAttributesNodeGen;
import com.oracle.truffle.r.nodes.binary.BoxPrimitiveNode;
import com.oracle.truffle.r.nodes.binary.BoxPrimitiveNodeGen;
import com.oracle.truffle.r.nodes.builtin.CastBuilder;
import com.oracle.truffle.r.nodes.builtin.RBuiltinNode;
import com.oracle.truffle.r.nodes.builtin.base.TrigExpFunctionsFactory.AcosNodeGen;
import com.oracle.truffle.r.nodes.builtin.base.TrigExpFunctionsFactory.AsinNodeGen;
import com.oracle.truffle.r.nodes.builtin.base.TrigExpFunctionsFactory.AtanNodeGen;
import com.oracle.truffle.r.nodes.builtin.base.TrigExpFunctionsFactory.CosNodeGen;
import com.oracle.truffle.r.nodes.builtin.base.TrigExpFunctionsFactory.SinNodeGen;
import com.oracle.truffle.r.nodes.builtin.base.TrigExpFunctionsFactory.TanNodeGen;
import com.oracle.truffle.r.nodes.unary.UnaryArithmeticBuiltinNode;
import com.oracle.truffle.r.runtime.RBuiltin;
import com.oracle.truffle.r.runtime.RBuiltinKind;
import com.oracle.truffle.r.runtime.RDispatch;
import com.oracle.truffle.r.runtime.RError;
import com.oracle.truffle.r.runtime.RInternalError;
import com.oracle.truffle.r.runtime.RRuntime;
import com.oracle.truffle.r.runtime.RType;
import com.oracle.truffle.r.runtime.data.RComplex;
import com.oracle.truffle.r.runtime.data.RDataFactory;
import com.oracle.truffle.r.runtime.data.RDoubleVector;
import com.oracle.truffle.r.runtime.data.RIntVector;
import com.oracle.truffle.r.runtime.data.RMissing;
import com.oracle.truffle.r.runtime.data.model.RAbstractDoubleVector;
import com.oracle.truffle.r.runtime.data.model.RAbstractVector;
import com.oracle.truffle.r.runtime.ops.BinaryArithmetic;
import com.oracle.truffle.r.runtime.ops.BinaryArithmetic.Pow.CHypot;
import com.oracle.truffle.r.runtime.ops.na.NACheck;

public class TrigExpFunctions {

    public abstract static class TrigExpFunctionNode extends RBuiltinNode {

        @Child private BoxPrimitiveNode boxPrimitive = BoxPrimitiveNodeGen.create();
        @Child private CHypot chypot;

        @Specialization
        protected Object calculateUnboxed(Object value) {
            return calculate(boxPrimitive.execute(value));
        }

        protected Object calculate(@SuppressWarnings("unused") Object value) {
            throw new UnsupportedOperationException();
        }

        private void ensureChypot() {
            if (chypot == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                chypot = insert(new CHypot());
            }
        }

        protected double hypot(double re, double im) {
            ensureChypot();
            return chypot.chypot(re, im);
        }
    }

    public abstract static class AdapterCall1 extends RBuiltinNode {

        @Child private BoxPrimitiveNode boxPrimitive = BoxPrimitiveNodeGen.create();

        private final BranchProfile notCompleteIntValueMet = BranchProfile.create();
        private final BranchProfile notCompleteDoubleValueMet = BranchProfile.create();
        private final NACheck na = NACheck.create();

        @Child private UnaryCopyAttributesNode copyAttributes = UnaryCopyAttributesNodeGen.create(true);

        @Specialization
        protected byte isType(@SuppressWarnings("unused") RMissing value) {
            CompilerDirectives.transferToInterpreter();
            throw RError.error(this, RError.Message.ARGUMENTS_PASSED_0_1, getRBuiltin().name());
        }

        protected double op(@SuppressWarnings("unused") double x) {
            // not abstract because this would confuse the DSL annotation
            // processor
            throw RInternalError.shouldNotReachHere("this method needs to be implemented in subclasses");
        }

        private double doFunInt(int value) {
            if (na.check(value)) {
                notCompleteIntValueMet.enter();
                return RRuntime.DOUBLE_NA;
            }
            return op(value);
        }

        private double doFunDouble(double value) {
            if (na.check(value)) {
                notCompleteDoubleValueMet.enter();
                return value;
            }
            return op(value);
        }

        @Specialization
        protected double trigOp(int x) {
            na.enable(x);
            return doFunInt(x);
        }

        @Specialization
        protected double trigOp(double x) {
            na.enable(x);
            return doFunDouble(x);
        }

        @Specialization
        protected RAbstractVector trigOp(RIntVector vector, //
                        @Cached("createCountingProfile()") LoopConditionProfile profile) {
            int length = vector.getLength();
            double[] resultVector = new double[length];
            reportWork(length);
            profile.profileCounted(length);
            na.enable(vector);
            for (int i = 0; profile.inject(i < length); i++) {
                resultVector[i] = doFunInt(vector.getDataAt(i));
            }
            return createDoubleVectorBasedOnOrigin(resultVector, vector);
        }

        @Specialization
        protected RAbstractVector trigOp(RDoubleVector vector, //
                        @Cached("createCountingProfile()") LoopConditionProfile profile) {
            int length = vector.getLength();
            double[] resultVector = new double[length];
            reportWork(length);
            profile.profileCounted(length);
            na.enable(vector);
            for (int i = 0; profile.inject(i < length); i++) {
                resultVector[i] = doFunDouble(vector.getDataAt(i));
            }
            return createDoubleVectorBasedOnOrigin(resultVector, vector);
        }

        private RAbstractVector createDoubleVectorBasedOnOrigin(double[] values, RAbstractVector originVector) {
            RDoubleVector result = RDataFactory.createDoubleVector(values, originVector.isComplete());
            return copyAttributes.execute(result, originVector);
        }
    }

    @RBuiltin(name = "exp", kind = RBuiltinKind.PRIMITIVE, parameterNames = {"x"}, dispatch = RDispatch.MATH_GROUP_GENERIC)
    public abstract static class Exp extends UnaryArithmeticBuiltinNode {

        public Exp() {
            super(RType.Double);
        }

        @Child private BinaryArithmetic calculatePowNode;

        @Override
        public double op(double op) {
            return Math.exp(op);
        }

        @Override
        public RComplex op(double re, double im) {
            if (calculatePowNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                calculatePowNode = insert(BinaryArithmetic.POW.create());
            }
            return calculatePowNode.op(Math.E, 0, re, im);
        }
    }

    @RBuiltin(name = "expm1", kind = RBuiltinKind.PRIMITIVE, parameterNames = {"x"}, dispatch = RDispatch.MATH_GROUP_GENERIC)
    public abstract static class ExpM1 extends UnaryArithmeticBuiltinNode {

        public ExpM1() {
            super(RType.Double);
        }

        @Child private BinaryArithmetic calculatePowNode;

        @Override
        public double op(double op) {
            return Math.expm1(op);
        }

        @Override
        public RComplex op(double re, double im) {
            if (calculatePowNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                calculatePowNode = insert(BinaryArithmetic.POW.create());
            }
            RComplex x = calculatePowNode.op(Math.E, 0, re, im);
            return RDataFactory.createComplex(x.getRealPart() - 1d, x.getImaginaryPart());
        }
    }

    @RBuiltin(name = "sin", kind = RBuiltinKind.PRIMITIVE, parameterNames = {"x"}, dispatch = RDispatch.MATH_GROUP_GENERIC)
    public abstract static class Sin extends UnaryArithmeticBuiltinNode {

        public Sin() {
            super(RType.Double);
        }

        @Override
        public double op(double op) {
            return Math.sin(op);
        }

        @Override
        public RComplex op(double re, double im) {
            double sinRe = Math.sin(re) * Math.cosh(im);
            double sinIm = Math.cos(re) * Math.sinh(im);
            return RDataFactory.createComplex(sinRe, sinIm);
        }
    }

    @RBuiltin(name = "sinh", kind = RBuiltinKind.PRIMITIVE, parameterNames = {"x"}, dispatch = RDispatch.MATH_GROUP_GENERIC)
    public abstract static class Sinh extends UnaryArithmeticBuiltinNode {

        public Sinh() {
            super(RType.Double);
        }

        @Override
        public double op(double op) {
            return Math.sinh(op);
        }

        @Override
        public RComplex op(double re, double im) {
            double sinhRe = Math.sinh(re) * Math.cos(im);
            double sinhIm = Math.cosh(re) * Math.sin(im);
            return RDataFactory.createComplex(sinhRe, sinhIm);
        }
    }

    @RBuiltin(name = "sinpi", kind = RBuiltinKind.PRIMITIVE, parameterNames = {"x"}, dispatch = RDispatch.MATH_GROUP_GENERIC)
    public abstract static class Sinpi extends UnaryArithmeticBuiltinNode {

        public Sinpi() {
            super(RType.Double);
        }

        @Override
        public double op(double op) {
            double norm = op % 2d;
            if (norm == 0d || norm == 1d || norm == -1d) {
                return 0d;
            }
            if (norm == -1.5d || norm == 0.5d) {
                return 1d;
            }
            if (norm == -0.5d || norm == 1.5d) {
                return -1d;
            }
            return Math.sin(norm * Math.PI);
        }

    }

    @RBuiltin(name = "cos", kind = RBuiltinKind.PRIMITIVE, parameterNames = {"x"}, dispatch = RDispatch.MATH_GROUP_GENERIC)
    public abstract static class Cos extends UnaryArithmeticBuiltinNode {

        public Cos() {
            super(RType.Double);
        }

        @Override
        public double op(double op) {
            return Math.cos(op);
        }

        @Override
        public RComplex op(double re, double im) {
            double cosRe = Math.cos(re) * Math.cosh(im);
            double cosIm = -Math.sin(re) * Math.sinh(im);
            return RDataFactory.createComplex(cosRe, cosIm);
        }
    }

    @RBuiltin(name = "cosh", kind = RBuiltinKind.PRIMITIVE, parameterNames = {"x"}, dispatch = RDispatch.MATH_GROUP_GENERIC)
    public abstract static class Cosh extends UnaryArithmeticBuiltinNode {

        public Cosh() {
            super(RType.Double);
        }

        @Override
        public double op(double op) {
            return Math.cosh(op);
        }

        @Override
        public RComplex op(double re, double im) {
            double cosRe = Math.cosh(re) * Math.cos(im);
            double cosIm = -Math.sinh(re) * Math.sin(im);
            return RDataFactory.createComplex(cosRe, cosIm);
        }
    }

    @RBuiltin(name = "cospi", kind = RBuiltinKind.PRIMITIVE, parameterNames = {"x"}, dispatch = RDispatch.MATH_GROUP_GENERIC)
    public abstract static class Cospi extends UnaryArithmeticBuiltinNode {

        public Cospi() {
            super(RType.Double);
        }

        @Override
        public double op(double op) {
            double norm = op % 2d;
            if (norm == 0d) {
                return 1d;
            }
            if (norm == -1d || norm == 1d) {
                return -1d;
            }
            if (norm == -1.5d || norm == -0.5d || norm == 0.5d || norm == 1.5d) {
                return 0d;
            }
            return Math.cos(norm * Math.PI);
        }

        @Override
        public RComplex op(double re, double im) {
            throw new UnsupportedOperationException();
        }
    }

    @RBuiltin(name = "tan", kind = RBuiltinKind.PRIMITIVE, parameterNames = {"x"}, dispatch = RDispatch.MATH_GROUP_GENERIC)
    public abstract static class Tan extends UnaryArithmeticBuiltinNode {

        public Tan() {
            super(RType.Double);
        }

        @Child private Sin sinNode = SinNodeGen.create(null);
        @Child private Cos cosNode = CosNodeGen.create(null);

        @Override
        public double op(double op) {
            return Math.tan(op);
        }

        @Override
        public RComplex op(double re, double im) {
            RComplex sin = sinNode.op(re, im);
            RComplex cos = cosNode.op(re, im);
            double denom = cos.getRealPart() * cos.getRealPart() + cos.getImaginaryPart() * cos.getImaginaryPart();
            double numRe = sin.getRealPart() * cos.getRealPart() + sin.getImaginaryPart() * cos.getImaginaryPart();
            double numIm = sin.getImaginaryPart() * cos.getRealPart() - sin.getRealPart() * cos.getImaginaryPart();
            return RDataFactory.createComplex(numRe / denom, numIm / denom);
        }
    }

    @RBuiltin(name = "tanh", kind = RBuiltinKind.PRIMITIVE, parameterNames = {"x"}, dispatch = RDispatch.MATH_GROUP_GENERIC)
    public abstract static class Tanh extends UnaryArithmeticBuiltinNode {

        public Tanh() {
            super(RType.Double);
        }

        @Child private Tan tanNode = TanNodeGen.create(null);

        @Override
        public double op(double op) {
            return Math.tanh(op);
        }

        @Override
        public RComplex op(double re, double im) {
            RComplex tan = tanNode.op(Math.PI + im, -re);
            return RDataFactory.createComplex(-tan.getImaginaryPart(), tan.getRealPart());
        }
    }

    @RBuiltin(name = "tanpi", kind = RBuiltinKind.PRIMITIVE, parameterNames = {"x"}, dispatch = RDispatch.MATH_GROUP_GENERIC)
    public abstract static class Tanpi extends UnaryArithmeticBuiltinNode {

        public Tanpi() {
            super(RType.Double);
        }

        @Override
        public double op(double op) {
            double norm = op % 1d;
            if (norm == 0d) {
                return 0d;
            }
            if (norm == -0.5d || norm == 0.5d) {
                return Double.NaN;
            }
            return Math.tan(norm * Math.PI);
        }

        @Override
        public RComplex op(double re, double im) {
            throw new UnsupportedOperationException();
        }
    }

    @RBuiltin(name = "asin", kind = RBuiltinKind.PRIMITIVE, parameterNames = {"x"}, dispatch = RDispatch.MATH_GROUP_GENERIC)
    public abstract static class Asin extends UnaryArithmeticBuiltinNode {

        @Child private CHypot chypot;

        public Asin() {
            super(RType.Double);
        }

        private void ensureChypot() {
            if (chypot == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                chypot = insert(new CHypot());
            }
        }

        protected double hypot(double re, double im) {
            ensureChypot();
            return chypot.chypot(re, im);
        }

        @Override
        public double op(double op) {
            return Math.asin(op);
        }

        // The code for complex asin is transcribed from FastR complex.c
        @Override
        public RComplex op(double x, double y) {
            if (y == 0 && Math.abs(x) > 1) {
                double t1 = 0.5 * Math.abs(x + 1);
                double t2 = 0.5 * Math.abs(x - 1);
                double alpha = t1 + t2;
                double ri = Math.log(alpha + Math.sqrt(alpha * alpha - 1));
                if (x > 1) {
                    ri *= -1;
                }
                return RDataFactory.createComplex(Math.asin(t1 - t2), ri);
            } else {
                return casin(x, y);
            }
        }

        private RComplex casin(double x, double y) {
            double t1 = 0.5 * hypot(x + 1, y);
            double t2 = 0.5 * hypot(x - 1, y);
            double alpha = t1 + t2;
            double ri = Math.log(alpha + Math.sqrt(alpha * alpha - 1));
            /*
             * This comes from 'z_asin() is continuous from below if x >= 1 and continuous from
             * above if x <= -1.'
             */
            if (y < 0 || (y == 0 && x > 1)) {
                ri *= -1;
            }
            return RDataFactory.createComplex(Math.asin(t1 - t2), ri);
        }
    }

    @RBuiltin(name = "asinh", kind = RBuiltinKind.PRIMITIVE, parameterNames = {"x"}, dispatch = RDispatch.MATH_GROUP_GENERIC)
    public abstract static class Asinh extends UnaryArithmeticBuiltinNode {

        @Child private Asin asinNode = AsinNodeGen.create(null);

        public Asinh() {
            super(RType.Double);
        }

        @Override
        public double op(double x) {
            return Math.log(x + Math.sqrt(x * x + 1d));
        }

        @Override
        public RComplex op(double re, double im) {
            RComplex asin = asinNode.op(-im, re);
            return RDataFactory.createComplex(asin.getImaginaryPart(), -asin.getRealPart());
        }
    }

    @RBuiltin(name = "acos", kind = RBuiltinKind.PRIMITIVE, parameterNames = {"x"}, dispatch = RDispatch.MATH_GROUP_GENERIC)
    public abstract static class Acos extends UnaryArithmeticBuiltinNode {

        public Acos() {
            super(RType.Double);
        }

        @Child private Asin asinNode = AsinNodeGen.create(null);

        @Override
        public double op(double op) {
            return Math.acos(op);
        }

        @Override
        public RComplex op(double re, double im) {
            RComplex asin = asinNode.op(re, im);
            return RDataFactory.createComplex(Math.PI / 2 - asin.getRealPart(), -asin.getImaginaryPart());
        }
    }

    @RBuiltin(name = "acosh", kind = RBuiltinKind.PRIMITIVE, parameterNames = {"x"}, dispatch = RDispatch.MATH_GROUP_GENERIC)
    public abstract static class Acosh extends UnaryArithmeticBuiltinNode {

        public Acosh() {
            super(RType.Double);
        }

        @Child private Acos acosNode = AcosNodeGen.create(null);

        @Override
        public double op(double x) {
            return Math.log(x + Math.sqrt(x * x - 1d));
        }

        @Override
        public RComplex op(double re, double im) {
            RComplex acos = acosNode.op(re, im);
            return RDataFactory.createComplex(-acos.getImaginaryPart(), acos.getRealPart());
        }
    }

    @RBuiltin(name = "atan", kind = RBuiltinKind.PRIMITIVE, parameterNames = {"x"}, dispatch = RDispatch.MATH_GROUP_GENERIC)
    public abstract static class Atan extends UnaryArithmeticBuiltinNode {

        public Atan() {
            super(RType.Double);
        }

        @Override
        public double op(double x) {
            return Math.atan(x);
        }

        @Override
        public RComplex op(double x, double y) {
            if (x == 0 && Math.abs(y) > 1) {
                double rr = (y > 0) ? Math.PI / 2 : -Math.PI / 2;
                double ri = 0.25 * Math.log(((y + 1) * (y + 1)) / ((y - 1) * (y - 1)));
                return RDataFactory.createComplex(rr, ri);
            } else {
                return catan(x, y);
            }
        }

        private static RComplex catan(double x, double y) {
            double rr = 0.5 * Math.atan2(2 * x, (1 - x * x - y * y));
            double ri = 0.25 * Math.log((x * x + (y + 1) * (y + 1)) / (x * x + (y - 1) * (y - 1)));
            return RDataFactory.createComplex(rr, ri);
        }
    }

    @RBuiltin(name = "atanh", kind = RBuiltinKind.PRIMITIVE, parameterNames = {"x"}, dispatch = RDispatch.MATH_GROUP_GENERIC)
    public abstract static class Atanh extends UnaryArithmeticBuiltinNode {

        public Atanh() {
            super(RType.Double);
        }

        @Child private Atan atanNode = AtanNodeGen.create(null);

        @Override
        public double op(double x) {
            return 0.5 * Math.log((1 + x) / (1 - x));
        }

        @Override
        public RComplex op(double x, double y) {
            RComplex atan = atanNode.op(y, -x);
            return RDataFactory.createComplex(-atan.getImaginaryPart(), atan.getRealPart());
        }
    }

    /**
     * {@code atan2} takes two args. To avoid combinatorial explosion in specializations we coerce
     * the {@code int} forms to {@code double}.
     */
    @RBuiltin(name = "atan2", kind = RBuiltinKind.INTERNAL, parameterNames = {"y", "x"})
    public abstract static class Atan2 extends RBuiltinNode {

        private final NACheck yNACheck = NACheck.create();
        private final NACheck xNACheck = NACheck.create();

        @Override
        protected void createCasts(CastBuilder casts) {
            casts.toDouble(0).toDouble(1);
        }

        private double doFunDouble(double y, double x) {
            double result = x;
            if (!yNACheck.check(y) && !xNACheck.check(x)) {
                result = Math.atan2(y, x);
            }
            return result;
        }

        @FunctionalInterface
        private interface IntDoubleFunction {

            double apply(int i);
        }

        private RDoubleVector doFun(int length, IntDoubleFunction yFun, IntDoubleFunction xFun,
                        LoopConditionProfile profile) {
            double[] resultVector = new double[length];
            reportWork(length);
            profile.profileCounted(length);
            for (int i = 0; profile.inject(i < length); i++) {
                double y = yFun.apply(i);
                double x = xFun.apply(i);
                if (xNACheck.check(y) || yNACheck.check(x)) {
                    resultVector[i] = RRuntime.DOUBLE_NA;
                } else {
                    resultVector[i] = Math.atan2(y, x);
                }
            }
            return RDataFactory.createDoubleVector(resultVector, xNACheck.neverSeenNA() && yNACheck.neverSeenNA());
        }

        @Specialization
        protected double atan2(double y, double x) {
            xNACheck.enable(x);
            yNACheck.enable(y);
            return doFunDouble(y, x);
        }

        @Specialization
        protected RDoubleVector atan2(double y, RAbstractDoubleVector x, //
                        @Cached("createCountingProfile()") LoopConditionProfile profile) {
            xNACheck.enable(x);
            yNACheck.enable(y);
            return doFun(x.getLength(), i -> y, i -> x.getDataAt(i), profile);
        }

        @Specialization
        protected RDoubleVector atan2(RAbstractDoubleVector y, double x, //
                        @Cached("createCountingProfile()") LoopConditionProfile profile) {
            xNACheck.enable(x);
            yNACheck.enable(y);
            return doFun(y.getLength(), i -> y.getDataAt(i), i -> x, profile);
        }

        @Specialization
        protected RDoubleVector atan2(RAbstractDoubleVector y, RAbstractDoubleVector x, //
                        @Cached("createCountingProfile()") LoopConditionProfile profile) {
            int xLength = x.getLength();
            int yLength = y.getLength();
            xNACheck.enable(x);
            yNACheck.enable(y);
            return doFun(Math.max(yLength, xLength), i -> y.getDataAt(i % yLength), i -> x.getDataAt(i % xLength),
                            profile);
        }

        @Fallback
        @TruffleBoundary
        protected Object atan2(Object x, Object y) {
            if (x instanceof RMissing) {
                throw RError.error(this, RError.Message.ARGUMENT_MISSING, getRBuiltin().parameterNames()[0]);
            } else if (y instanceof RMissing) {
                throw RError.error(this, RError.Message.ARGUMENT_MISSING, getRBuiltin().parameterNames()[1]);
            }
            throw RInternalError.unimplemented();
        }
    }
}
