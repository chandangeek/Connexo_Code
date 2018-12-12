/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties;

import com.elster.jupiter.util.units.Quantity;

import java.math.BigDecimal;
import java.sql.Types;

public class QuantityValueFactory extends AbstractValueFactory<Quantity> {

    public static final String VALUE_UNIT_SEPARATOR = ":";

    @Override
    protected int getJdbcType() {
        return Types.VARCHAR;
    }

    @Override
    public Quantity fromStringValue(String stringValue) {
        return getValueFromObject(stringValue);
    }

    @Override
    public String toStringValue(Quantity object) {
        return getObjectFromValue(object);
    }

    @Override
    public Class<Quantity> getValueType() {
        return Quantity.class;
    }

    @Override
    public Quantity valueFromDatabase(Object object) {
        return this.getValueFromObject(object);
    }

    @Override
    public Object valueToDatabase(Quantity object) {
        return getObjectFromValue(object);
    }

    private Quantity getValueFromObject(final Object object) {
        if (object != null) {
            String value = (String) object;
            String[] quantityParameters = value.split(VALUE_UNIT_SEPARATOR);
            if (quantityParameters.length == 3) {
                BigDecimal bigDecimalValue;
                if ("null".equals(quantityParameters[0])) {
                    bigDecimalValue = BigDecimal.ZERO;
                } else {
                    bigDecimalValue = new BigDecimal(quantityParameters[0]);
                }
                int multiplier = Integer.parseInt(quantityParameters[1]);
                String unit = quantityParameters[2];
                return Quantity.create(bigDecimalValue, multiplier, unit);
            }
        }

        return null;
    }

    private String getObjectFromValue(final Quantity quantity) {
        if (quantity == null) {
            return null;
        }
        return quantity.getValue() + VALUE_UNIT_SEPARATOR + quantity.getMultiplier() +
                VALUE_UNIT_SEPARATOR + quantity.getUnit().getAsciiSymbol();
    }
}