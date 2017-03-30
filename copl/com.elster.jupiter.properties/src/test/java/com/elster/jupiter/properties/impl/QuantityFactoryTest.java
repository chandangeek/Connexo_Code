/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties.impl;

import com.elster.jupiter.properties.QuantityValueFactory;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.util.units.Unit;

import java.math.BigDecimal;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class QuantityFactoryTest {

    @Test
    public void testFromStringValue() {
        QuantityValueFactory quantityValueFactory = new QuantityValueFactory();
        String properlyFormattedValue = "12:1:m";
        Quantity expectedProperQuantity = quantityValueFactory.fromStringValue(properlyFormattedValue);

        assertThat(expectedProperQuantity.getUnit() == Unit.METER);
        assertThat(expectedProperQuantity.getMultiplier() == 1);
        assertThat(expectedProperQuantity.getValue().equals(new BigDecimal(12)));
    }

    @Test
    public void testToStringValue() {
        QuantityValueFactory quantityValueFactory = new QuantityValueFactory();
        Quantity quantity = Quantity.create(BigDecimal.valueOf(2000), 3, "Wh");
        String expected = "2000:3:Wh";
        String result = quantityValueFactory.toStringValue(quantity);

        assertThat(result != null);
        assertThat(expected.equals(result));
    }
}
