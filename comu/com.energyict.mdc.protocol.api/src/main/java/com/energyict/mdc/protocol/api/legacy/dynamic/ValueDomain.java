package com.energyict.mdc.protocol.api.legacy.dynamic;

import java.io.Serializable;

/**
 * A ValueDomain describes the possible content of an attribute.
 *
 * @author Steven Willems.
 * @since Apr 21, 2009.
 */
public class ValueDomain implements Serializable {

    private Class valueType;

    /**
     * Create a ValueDomain with just a value type. (i.e. no referenced objects, no lookup value).
     *
     * @param valueType E.g. 'java.lang.String.class' or 'com.energyict.ean.Ean18.class'.
     */
    public ValueDomain(Class valueType) {
        this.valueType = valueType;
    }

    public Class getValueType() {
        return valueType;
    }

    /**
     * Flag that indicates if the attribute references an other Business Object.
     *
     * @return True if the attribute is a reference, false otherwise.
     */
    public boolean isReference() {
        return false;
    }

}