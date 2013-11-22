package com.energyict.mdc.rest.impl.properties.validators;

import com.energyict.mdc.rest.impl.properties.PropertyValidationRule;
import com.google.common.base.Optional;

/**
 * Defines rules/options to validate a String
 *
 * Copyrights EnergyICT
 * Date: 19/11/13
 * Time: 13:35
 */
public class StringValidationRules implements PropertyValidationRule {

    public static final String HEX_CHARACTERS = "[A-Fa-f0-9]";
    public static final String LOWER_ALPHABET = "[a-z]";
    public static final String UPPER_ALPHABET = "[A-Z]";
    public static final String ALPHABET = "[A-Za-z]";
    public static final String ALPHANUMERIC = "[A-Za-z0-9]";

    final Optional<Integer> minLength;
    final Optional<Integer> maxLength;
    final Optional<Integer> enforceMaxLength;
    final Optional<Boolean> evenLength;
    final Optional<String> regex;

    public StringValidationRules(Optional<Integer> minLength, Optional<Integer> maxLength, Optional<Integer> enforceMaxLength, Optional<Boolean> evenLength, Optional<String> regex) {
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.enforceMaxLength = enforceMaxLength;
        this.evenLength = evenLength;
        this.regex = regex;
    }

    public Optional<Integer> getMinLength() {
        return minLength;
    }

    public Optional<Integer> getMaxLength() {
        return maxLength;
    }

    public Optional<Integer> getEnforceMaxLength() {
        return enforceMaxLength;
    }

    public Optional<Boolean> getEvenLength() {
        return evenLength;
    }

    public Optional<String> getRegex() {
        return regex;
    }
}
