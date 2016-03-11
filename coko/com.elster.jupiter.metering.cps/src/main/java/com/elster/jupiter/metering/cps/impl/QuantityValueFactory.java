package com.elster.jupiter.metering.cps.impl;

import com.elster.jupiter.properties.AbstractValueFactory;
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
        if (object == null) {
            return null;
        } else {
            String value = (String) object;
            String[] quantityParameters = value.split(VALUE_UNIT_SEPARATOR);
            if (quantityParameters.length < 1) {
                return null;
            }

            BigDecimal bigDecimalValue = new BigDecimal(quantityParameters[0]);
            int multiplier;
            String unit;
            if (quantityParameters.length == 3) {
                multiplier = Integer.parseInt(quantityParameters[1]);
                unit = quantityParameters[2];
                return Quantity.create(bigDecimalValue, multiplier, unit);
            }

            unit = quantityParameters[1];

            try {
                Quantity.create(bigDecimalValue, unit);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }

            return Quantity.create(bigDecimalValue, unit);
        }

    }

    private String getObjectFromValue(final Quantity quantity) {
        if (quantity == null) {
            return null;
        }
        return quantity.getValue() + VALUE_UNIT_SEPARATOR + quantity.getMultiplier() +
                VALUE_UNIT_SEPARATOR + quantity.getUnit();
    }
}
