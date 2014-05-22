/*
 * Copyright (c) 2014, 2014, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.truffle.r.runtime.data;

import com.oracle.truffle.r.runtime.*;

/**
 * Denotes an R type that can have associated attributes, e.g. {@link RVector}, {@link REnvironment}
 *
 * An attribute is a {@code String, Object} pair. The set of attributes associated with an
 * {@link RAttributable} is implemented by the {@link RAttributes} class.
 */
public interface RAttributable {
    /**
     * If the attribute set is not initialized, then initialize it.
     */
    void initAttributes();

    /**
     * Access all the attributes. Use {@code for (RAttribute a : getAttributes) ... }. Returns
     * {@code null} if not initialized.
     */
    RAttributes getAttributes();

    /**
     * Set the attribute {@code name} to {@code value}, overwriting any existing value. This is
     * generic; a class may need to override this to handle certain attributes specially,
     */
    default void setAttr(String name, Object value) {
        if (getAttributes() == null) {
            initAttributes();
        }
        getAttributes().put(name, value);
    }

    /**
     * Return the attribute {@code name} or {@code null} if not found. This is generic; a class may
     * need to override this to handle certain attributes specially,
     */
    default Object getAttr(String name) {
        RAttributes attributes = getAttributes();
        if (attributes == null) {
            return null;
        } else {
            return attributes.get(name);
        }
    }

    /**
     * Remove the attribute {@code name}. No error if {@code name} is not an attribute. This is
     * generic; a class may need to override this to handle certain attributes specially,
     */
    default void removeAttr(String name) {
        RAttributes attributes = getAttributes();
        if (attributes != null) {
            attributes.remove(name);
        }
    }

}