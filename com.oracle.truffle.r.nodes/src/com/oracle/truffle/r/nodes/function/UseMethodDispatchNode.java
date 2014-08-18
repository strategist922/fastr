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

package com.oracle.truffle.r.nodes.function;

import java.util.*;

import com.oracle.truffle.api.CompilerDirectives.SlowPath;
import com.oracle.truffle.api.*;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.frame.FrameInstance.FrameAccess;
import com.oracle.truffle.r.runtime.*;
import com.oracle.truffle.r.runtime.data.*;

public class UseMethodDispatchNode extends S3DispatchNode {

    private final String[] suppliedArgsNames;

    UseMethodDispatchNode(final String generic, final RStringVector type, String[] suppliedArgsNames) {
        this.genericName = generic;
        this.type = type;
        this.suppliedArgsNames = suppliedArgsNames;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        Frame callerFrame = Utils.getCallerFrame(FrameAccess.MATERIALIZE);
        if (targetFunction == null) {
            findTargetFunction(frame, callerFrame);
        }
        return executeHelper(frame, callerFrame, extractArgs(frame));
    }

    @Override
    public Object execute(VirtualFrame frame, RStringVector aType) {
        this.type = aType;
        Frame callerFrame = Utils.getCallerFrame(FrameAccess.MATERIALIZE);
        findTargetFunction(frame, callerFrame);
        return executeHelper(frame, callerFrame, extractArgs(frame));
    }

    @Override
    public Object executeInternal(VirtualFrame frame, Object[] args) {
        if (targetFunction == null) {
            findTargetFunction(frame, frame);
        }
        return executeHelper(frame, frame, args);
    }

    @Override
    public Object executeInternal(VirtualFrame frame, RStringVector aType, Object[] args) {
        this.type = aType;
        findTargetFunction(frame, frame);
        return executeHelper(frame, frame, args);
    }

    private Object executeHelper(VirtualFrame frame, Frame callerFrame, Object[] args) {
        // Extract arguments from current frame...
        int argCount = args.length;
        int argListSize = argCount;
        ArrayList<Object> argList = new ArrayList<>(argListSize);
        int fi = 0;
        for (; fi < argCount; ++fi) {
            Object arg = args[fi];
            if (arg instanceof Object[]) {
                Object[] varArgs = (Object[]) arg;
                argListSize += varArgs.length;
                argList.ensureCapacity(argListSize);

                for (Object varArg : varArgs) {
                    addArg(frame, argList, varArg);
                }
            } else {
                addArg(frame, argList, arg);
            }
        }

        // ...and use them as 'supplied' arguments...
        String[] calledSuppliedNames = suppliedArgsNames;
        // TODO Need rearrange here! suppliedArgsNames are in supplied order, argList in formal!!!
        EvaluatedArguments evaledArgs = EvaluatedArguments.create(argList.toArray(), calledSuppliedNames);
        // ...to match them against the chosen function's formal arguments
        EvaluatedArguments reorderedArgs = ArgumentMatcher.matchArgumentsEvaluated(frame, targetFunction, evaledArgs, getEncapsulatingSourceSection());
        return executeHelper2(callerFrame, reorderedArgs.getEvaluatedArgs());
    }

    private static Object[] extractArgs(VirtualFrame frame) {
        Object[] args = new Object[RArguments.getArgumentsLength(frame)];
        for (int i = 0; i < args.length; ++i) {
            args[i] = RArguments.getArgument(frame, i);
        }
        return args;
    }

    private static void addArg(VirtualFrame frame, List<Object> values, Object value) {
        if (RMissingHelper.isMissing(frame, value)) {
            values.add(null);
        } else {
            values.add(value);
        }
    }

    @SlowPath
    private Object executeHelper2(Frame callerFrame, Object[] arguments) {
        Object[] argObject = RArguments.createS3Args(targetFunction, arguments);
        VirtualFrame newFrame = Truffle.getRuntime().createVirtualFrame(argObject, new FrameDescriptor());
        genCallEnv = callerFrame;
        defineVarsNew(newFrame);
        RArguments.setS3Method(newFrame, targetFunctionName);
        return funCallNode.call(newFrame, targetFunction.getTarget(), argObject);
    }

    private void findTargetFunction(VirtualFrame frame, Frame callerFrame) {
        findTargetFunctionLookup(callerFrame);
        if (targetFunction == null) {
            throw RError.error(frame, getEncapsulatingSourceSection(), RError.Message.UNKNOWN_FUNCTION_USE_METHOD, this.genericName, RRuntime.toString(this.type));
        }
    }

    @SlowPath
    private void findTargetFunctionLookup(Frame callerFrame) {
        for (int i = 0; i < this.type.getLength(); ++i) {
            findFunction(this.genericName, this.type.getDataAt(i), callerFrame);
            if (targetFunction != null) {
                RStringVector classVec = null;
                if (i > 0) {
                    isFirst = false;
                    classVec = RDataFactory.createStringVector(Arrays.copyOfRange(this.type.getDataWithoutCopying(), i, this.type.getLength()), true);
                    classVec.setAttr(RRuntime.PREVIOUS_ATTR_KEY, this.type.copyResized(this.type.getLength(), false));
                } else {
                    isFirst = true;
                    classVec = this.type.copyResized(this.type.getLength(), false);
                }
                klass = classVec;
                break;
            }
        }
        if (targetFunction != null) {
            return;
        }
        findFunction(this.genericName, RRuntime.DEFAULT, callerFrame);
    }
}