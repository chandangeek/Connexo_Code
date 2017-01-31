/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties.rest;


public class StringValidationRules implements PropertyValidationRule {

    public static final String HEX_CHARACTERS_REGEX = "[A-Fa-f0-9]";
    public static final String LOWER_ALPHABET_REGEX = "[a-z]";
    public static final String UPPER_ALPHABET_REGEX = "[A-Z]";
    public static final String ALPHABET_REGEX = "[A-Za-z]";
    public static final String ALPHANUMERIC_REGEX = "[A-Za-z0-9]";

    public Integer minLength;
    public Integer maxLength;
    public Boolean enforceMaxLength;
    public Boolean evenLength;
    public String regex;

    public Integer getMinLength() {
        return minLength;
    }

    public void setMinLength(Integer minLength) {
        this.minLength = minLength;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    public Boolean getEnforceMaxLength() {
        return enforceMaxLength;
    }

    public void setEnforceMaxLength(Boolean enforceMaxLength) {
        this.enforceMaxLength = enforceMaxLength;
    }

    public Boolean getEvenLength() {
        return evenLength;
    }

    public void setEvenLength(Boolean evenLength) {
        this.evenLength = evenLength;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }
}
