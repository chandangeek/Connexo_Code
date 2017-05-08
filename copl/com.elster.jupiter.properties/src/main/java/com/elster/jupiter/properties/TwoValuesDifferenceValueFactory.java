/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties;

import java.math.BigDecimal;

public class TwoValuesDifferenceValueFactory extends AbstractValueFactory<TwoValuesDifference> {

    private static final String ABSOLUTE_VALUE_TAG = "absolute:";
    private static final String RELATIVE_VALUE_TAG = "relative:";

    @Override
    protected int getJdbcType() {
        return java.sql.Types.VARCHAR;
    }

    @Override
    public TwoValuesDifference fromStringValue(String stringValue) {
        if (stringValue.startsWith(ABSOLUTE_VALUE_TAG)) {
            return new TwoValuesDifference(TwoValuesDifference.Type.ABSOLUTE, new BigDecimal(stringValue.replaceAll(ABSOLUTE_VALUE_TAG, "")));
        }
        if (stringValue.startsWith(RELATIVE_VALUE_TAG)) {
            return new TwoValuesDifference(TwoValuesDifference.Type.RELATIVE, new BigDecimal(stringValue.replaceAll(RELATIVE_VALUE_TAG, "")));
        }
        return null;
    }

    @Override
    public String toStringValue(TwoValuesDifference object) {
        switch (object.getType()) {
            case ABSOLUTE:
                return ABSOLUTE_VALUE_TAG + object.getValue();
            case RELATIVE:
                return RELATIVE_VALUE_TAG + object.getValue();
            default:
                throw new IllegalArgumentException("Unsupported type of difference:" + object.getType().name());
        }
    }

    @Override
    public Class<TwoValuesDifference> getValueType() {
        return TwoValuesDifference.class;
    }

    @Override
    public TwoValuesDifference valueFromDatabase(Object object) {
        if (object != null) {
            String value = (String) object;
            return fromStringValue(value);
        } else {
            return null;
        }
    }

    @Override
    public Object valueToDatabase(TwoValuesDifference object) {
        return toStringValue(object);
    }
}
