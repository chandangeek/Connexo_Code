package com.energyict.mdc.rest.impl.properties.validators;

import com.energyict.mdc.rest.impl.properties.PropertyValidationRule;
import com.google.common.base.Optional;

/**
 * Defines rules/options to validate a Number
 *
 * Copyrights EnergyICT
 * Date: 19/11/13
 * Time: 13:28
 */
public class NumberValidationRules<T> implements PropertyValidationRule {

    final Optional<Boolean> allowDecimals;
    final Optional<T> minimumValue;
    final Optional<T> maximumValue;
    final Optional<Integer> minimumDigits;
    final Optional<Integer> maximumDigits;
    final Optional<Boolean> even;

    public NumberValidationRules(Optional<Boolean> allowDecimals, Optional<T> minimumValue, Optional<T> maximumValue, Optional<Integer> minimumDigits, Optional<Integer> maximumDigits, Optional<Boolean> even) {
        this.allowDecimals = allowDecimals;
        this.minimumValue = minimumValue;
        this.maximumValue = maximumValue;
        this.minimumDigits = minimumDigits;
        this.maximumDigits = maximumDigits;
        this.even = even;
    }

    public Optional<Boolean> getAllowDecimals() {
        return allowDecimals;
    }

    public Optional<T> getMinimumValue() {
        return minimumValue;
    }

    public Optional<T> getMaximumValue() {
        return maximumValue;
    }

    public Optional<Integer> getMinimumDigits() {
        return minimumDigits;
    }

    public Optional<Integer> getMaximumDigits() {
        return maximumDigits;
    }

    public Optional<Boolean> getEven() {
        return even;
    }
}
