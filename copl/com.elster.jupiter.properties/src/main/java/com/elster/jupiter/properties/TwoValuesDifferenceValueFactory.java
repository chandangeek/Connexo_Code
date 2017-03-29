/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties;

import java.math.BigDecimal;

/**
 * Created by dantonov on 28.03.2017.
 */
public class TwoValuesDifferenceValueFactory extends AbstractValueFactory<TwoValuesDifference> {

    private static final String ABSOLUTE_VALUE_TAG = "Absolute:";
    private static final String PERCENT_VALUE_TAG = "Percent:";

    @Override
    protected int getJdbcType() {
        return java.sql.Types.VARCHAR;
    }

    @Override
    public TwoValuesDifference fromStringValue(String stringValue) {
        if (stringValue.contains(ABSOLUTE_VALUE_TAG)) {
            TwoValuesAbsoluteDifference diffValue = new TwoValuesAbsoluteDifference();
            diffValue.value = new BigDecimal(stringValue.replaceAll(ABSOLUTE_VALUE_TAG, ""));
            return diffValue;
        } else if (stringValue.contains(PERCENT_VALUE_TAG)) {
            TwoValuesPercentDifference diffValue = new TwoValuesPercentDifference();
            diffValue.value = Double.valueOf(stringValue.replaceAll(PERCENT_VALUE_TAG, ""));
            return diffValue;
        } else {
            return null;
        }
    }

    @Override
    public String toStringValue(TwoValuesDifference object) {
        if (object instanceof TwoValuesAbsoluteDifference) {
            return ABSOLUTE_VALUE_TAG + ((TwoValuesAbsoluteDifference) object).value;
        } else if (object instanceof TwoValuesPercentDifference) {
            return PERCENT_VALUE_TAG + ((TwoValuesPercentDifference) object).value;
        }
        return null;
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
