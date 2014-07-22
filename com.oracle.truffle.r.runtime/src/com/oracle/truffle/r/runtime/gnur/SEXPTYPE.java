/*
 * This material is distributed under the GNU General Public License
 * Version 2. You may review the terms of this license at
 * http://www.gnu.org/licenses/gpl-2.0.html
 *
 * Copyright (c) 1995-2012, The R Core Team
 * Copyright (c) 2003, The R Foundation
 * Copyright (c) 2013, 2014, Oracle and/or its affiliates
 *
 * All rights reserved.
 */
package com.oracle.truffle.r.runtime.gnur;

import java.util.*;

import com.oracle.truffle.r.runtime.*;
import com.oracle.truffle.r.runtime.data.*;

// Transcribed from GnuR src/include/Rinternals.h and src/main/serialize.c

public enum SEXPTYPE {
    NILSXP(0, RNull.class), /* nil ()NULL */
    SYMSXP(1, RSymbol.class), /* symbols */
    LISTSXP(2, RPairList.class), /* lists of dotted pairs */
    CLOSXP(3, RPairList.class), /* closures */
    ENVSXP(4, REnvironment.class), /* environments */
    PROMSXP(5, RPromise.class), /* promises: [un]evaluated closure arguments */
    LANGSXP(6, RLanguage.class), /* language constructs (special lists) */
    SPECIALSXP(7, null), /* special forms */
    BUILTINSXP(8, null), /* builtin non-special forms */
    CHARSXP(9, String.class), /* "scalar" string type (internal only) */
    LGLSXP(10, RLogicalVector.class), /* logical vectors */
    INTSXP(13, RIntVector.class), /* integer vectors */
    REALSXP(14, RDoubleVector.class), /* real variables */
    CPLXSXP(15, RComplexVector.class), /* complex variables */
    STRSXP(16, RStringVector.class), /* string vectors */
    DOTSXP(17, RPairList.class), /* dot-dot-dot object */
    ANYSXP(18, null), /* make "any" args work */
    VECSXP(19, RList.class), /* generic vectors */
    EXPRSXP(20, RExpression.class), /* expressions vectors */
    BCODESXP(21, null), /* byte code */
    EXTPTRSXP(22, null), /* external pointer */
    WEAKREFSXP(23, null), /* weak reference */
    RAWSXP(24, RRawVector.class), /* raw bytes */
    S4SXP(25, null), /* S4 non-vector */

    NEWSXP(30, null), /* fresh node created in new page */
    FREESXP(31, null), /* node released by GC */

    FUNSXP(99, null), /* Closure or Builtin */

    // used in RSerialize
    REFSXP(255, null),
    NILVALUE_SXP(254, null),
    GLOBALENV_SXP(253, null),
    UNBOUNDVALUE_SXP(252, null),
    MISSINGARG_SXP(251, null),
    BASENAMESPACE_SXP(250, null),
    NAMESPACESXP(249, null),
    PACKAGESXP(248, null),
    PERSISTSXP(247, null),
    EMPTYENV_SXP(242, null),
    BASEENV_SXP(241, null),

    // FastR scalar variants of GnuR vector types
    FASTR_DOUBLE(300, Double.class),
    FASTR_INT(301, Integer.class),
    FASTR_BYTE(302, Byte.class),
    FASTR_STRING(303, String.class);

    public final int code;
    public final Class<?> fastRClass;

    private static final SEXPTYPE[] VALUES = values();

    SEXPTYPE(int code, Class<?> fastRClass) {
        this.code = code;
        this.fastRClass = fastRClass;
    }

    public static final Map<Integer, SEXPTYPE> codeMap = new HashMap<>();

    /**
     * Return the GnuR type for the FastR class. There are times when it is convenient to work with
     * ints, e.g. {@code DeParse}. N.B. This is not unique for {@link RPairList}, so the
     * {@code type} field on the {@link RPairList} has to be consulted.
     */
    public static SEXPTYPE typeForClass(Class<?> fastRClass) {
        for (SEXPTYPE type : VALUES) {
            if (fastRClass == type.fastRClass) {
                return type;
            }
        }
        assert false;
        return null;
    }

    public static SEXPTYPE convertFastRScalarType(SEXPTYPE type) {
        switch (type) {
            case FASTR_DOUBLE:
                return SEXPTYPE.REALSXP;
            case FASTR_INT:
                return SEXPTYPE.INTSXP;
            case FASTR_BYTE:
                return SEXPTYPE.LGLSXP;
            case FASTR_STRING:
                return SEXPTYPE.CHARSXP;
            default:
                assert false;
                return null;
        }
    }

    static {
        for (SEXPTYPE type : SEXPTYPE.values()) {
            SEXPTYPE.codeMap.put(type.code, type);
        }
    }

}