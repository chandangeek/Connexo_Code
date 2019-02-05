/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cim.webservices.inbound.soap.impl;

import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.util.units.Unit;

import java.math.BigDecimal;

public class XsdQuantityConverter {
    public static final String VALUE_UNIT_SEPARATOR = ":";

    public static Quantity unmarshal(String value) throws ValueParserException {
        if (!Checks.is(value).emptyOrOnlyWhiteSpace()) {
            String[] quantityParameters = value.split(VALUE_UNIT_SEPARATOR);
            if (quantityParameters.length == 3) {
                try {
                    BigDecimal bigDecimalValue = new BigDecimal(quantityParameters[0]);
                    int multiplier = Integer.parseInt(quantityParameters[1]);
                    String unitString = quantityParameters[2];
                    try {
                        Unit.get(unitString);
                    } catch (IllegalArgumentException e) {
                        throw new ValueParserException(unitString);
                    }
                    return Quantity.create(bigDecimalValue, multiplier, unitString);
                } catch (IllegalArgumentException e) {
                    throw new ValueParserException(value);
                }
            } else {
                throw new ValueParserException(value);
            }
        } else {
            return null;
        }
    }

    public static String marshal(Quantity quantity) {
        return quantity.getValue() + VALUE_UNIT_SEPARATOR + quantity.getMultiplier() + VALUE_UNIT_SEPARATOR + quantity.getUnit();

    }

}
