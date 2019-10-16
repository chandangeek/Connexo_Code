package com.energyict.mdc.sap.soap.custom.custompropertyset;

import com.elster.jupiter.properties.BigDecimalFactory;

import java.math.BigDecimal;

/**
 * Provides an implementation for the {@link BigDecimalFactory} interface
 * that validates that the percent value is not out of range (0-100).
 */
final class PercentBigDecimalFactory extends BigDecimalFactory {
    @Override
    public boolean isValid(BigDecimal value) {
        return value.doubleValue() >= 0 && value.doubleValue() <= 100;
    }
}
