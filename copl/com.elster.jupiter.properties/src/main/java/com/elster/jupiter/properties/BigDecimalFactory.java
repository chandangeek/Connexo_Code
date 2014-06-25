package com.elster.jupiter.properties;

import java.math.BigDecimal;

/**
 * Provides an implementation for the {@link ValueFactory} interface
 * for BigDecimal values.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-29 (16:47)
 */
public class BigDecimalFactory implements ValueFactory<BigDecimal> {

    @Override
    public Class<BigDecimal> getValueType () {
        return BigDecimal.class;
    }

    @Override
    public BigDecimal fromStringValue (String stringValue) {
        if (stringValue == null) {
            return null;
        }
        else {
            return new BigDecimal(stringValue);
        }
    }

    @Override
    public String toStringValue (BigDecimal object) {
        if (object == null) {
            return "";
        }
        else {
            return object.toString();
        }
    }
    
    @Override
    public boolean isReference() {
        return false;
    }
}