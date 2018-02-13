/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.upl;

/**
 * A subclass of TypedProperties which is unmodifiable.
 *
 * @author Joost Bruneel (jbr), Rudi Vankeirsbilck (rudi)
 * @since 2012-05-10 (14:11)
 */
public class UnmodifiableTypedProperties extends TypedProperties {

    public UnmodifiableTypedProperties(TypedProperties delegate) {
        super.setAllProperties(delegate);
    }

    @Override
    public void setProperty(String propertyName, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeProperty(String propertyName) {
        throw new UnsupportedOperationException();
    }

}