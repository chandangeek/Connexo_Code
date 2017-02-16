/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.rest.impl;

public enum ReadingTypeFields {
    MACRO_PERIOD(1, "macroPeriod"),
    AGGREGATE(2, "aggregate"),
    MEASUREMENT_PERIOD(3, "measurementPeriod"),
    ACCUMULATION(4, "accumulation"),
    FLOW_DIRECTION(5, "flowDirection"),
    COMMODITY(6, "commodity"),
    MEASUREMENT_KIND(7, "measurementKind"),
    INTERHARMONIC_NUMERATOR(8, "interHarmonicNumerator"),
    INTERHARMONIC_DENOMINATOR(9, "interHarmonicDenominator"),
    ARGUMENT_NUMERATOR(10, "argumentNumerator"),
    ARGUMENT_DENOMINATOR(11, "argumentDenominator"),
    TIME_OF_USE(12, "timeOfUse"),
    CPP(13, "criticalPeakPeriod"),
    CONSUMPTION_TIER(14, "consumptionTier"),
    PHASES(15, "phases"),
    MULTIPLIER(16, "metricMultiplier"),
    UNIT(17, "unit"),
    CURRENCY(18, "currency");

    private final int id;
    private final String fieldName;

    ReadingTypeFields(int id, String fieldName) {
        this.id = id;
        this.fieldName = fieldName;
    }

    public int getId() {
        return id;
    }

    public String getFieldName() {
        return fieldName;
    }
}