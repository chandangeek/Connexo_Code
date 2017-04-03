/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties;

import java.math.BigDecimal;

/**
 * Created by dantonov on 28.03.2017.
 */
public class NonOrBigDecimalValueFactory extends AbstractValueFactory<NonOrBigDecimalValueProperty> {

    private static final String NONE_VALUE = "NONE";
    private static final String NOT_NONE_VALUE = "VALUE:";

    @Override
    protected int getJdbcType() {
        return java.sql.Types.VARCHAR;
    }

    @Override
    public NonOrBigDecimalValueProperty fromStringValue(String stringValue) {
        if (stringValue.equals(NONE_VALUE)) {
            return new NonOrBigDecimalValueProperty();
        } else {
            return new NonOrBigDecimalValueProperty(new BigDecimal(stringValue.replaceAll(NOT_NONE_VALUE, "")));
        }
    }

    @Override
    public String toStringValue(NonOrBigDecimalValueProperty object) {
        if (object.isNone) {
            return NONE_VALUE;
        } else {
            return NOT_NONE_VALUE + String.valueOf(object.value);
        }
    }

    @Override
    public Class<NonOrBigDecimalValueProperty> getValueType() {
        return NonOrBigDecimalValueProperty.class;
    }

    @Override
    public NonOrBigDecimalValueProperty valueFromDatabase(Object object) {
        if (object != null) {
            String value = (String) object;
            return fromStringValue(value);
        } else {
            return null;
        }
    }

    @Override
    public Object valueToDatabase(NonOrBigDecimalValueProperty object) {
        return toStringValue(object);
    }
}
