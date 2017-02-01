/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

/**
* Defines fields that apply to both {@link NumericalRegisterSpecImpl} and {@link TextualRegisterSpecImpl}.
*
* @author Rudi Vankeirsbilck (rudi)
* @since 2014-07-29 (13:10)
*/
enum RegisterSpecFields {
    REGISTER_TYPE("registerType"),
    NUMBER_OF_FRACTION_DIGITS("numberOfFractionDigits"),
    OVERFLOW_VALUE("overflow"),
    TEXTUAL("textual"),
    USEMULTIPLIER("useMultiplier"),
    CALCULATED_READINGTYPE("calculatedReadingType");

    private final String javaFieldName;

    RegisterSpecFields(String javaFieldName) {
        this.javaFieldName = javaFieldName;
    }

    String fieldName() {
        return javaFieldName;
    }
}