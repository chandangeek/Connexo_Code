/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties;

import java.math.BigDecimal;

public class NoneOrBigDecimalValueFactory extends AbstractValueFactory<NoneOrBigDecimal> {

    private static final String NONE = "none";

    @Override
    protected int getJdbcType() {
        return java.sql.Types.VARCHAR;
    }

    @Override
    public NoneOrBigDecimal fromStringValue(String stringValue) {
        if (NONE.equals(stringValue)) {
            return NoneOrBigDecimal.none();
        }
        return NoneOrBigDecimal.of(new BigDecimal(stringValue));
    }

    @Override
    public String toStringValue(NoneOrBigDecimal object) {
        if (object.isNone()) {
            return NONE;
        }
        return String.valueOf(object.getValue());
    }

    @Override
    public Class<NoneOrBigDecimal> getValueType() {
        return NoneOrBigDecimal.class;
    }

    @Override
    public NoneOrBigDecimal valueFromDatabase(Object object) {
        if (object != null) {
            String value = (String) object;
            return fromStringValue(value);
        } else {
            return null;
        }
    }

    @Override
    public Object valueToDatabase(NoneOrBigDecimal object) {
        return toStringValue(object);
    }
}
