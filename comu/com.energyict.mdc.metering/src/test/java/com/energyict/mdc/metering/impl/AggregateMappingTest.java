/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.metering.impl;

import com.elster.jupiter.cbo.Aggregate;
import com.elster.jupiter.cbo.MacroPeriod;
import com.energyict.mdc.common.ObisCode;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class AggregateMappingTest {

    private final MacroPeriod macroPeriod = MacroPeriod.DAILY;

    @Test
    public void nullSafeObisCodeTest() {
        ObisCode nullSafe = null;
        Aggregate measurementKind = AggregateMapping.getAggregateFor(nullSafe, macroPeriod);
        assertThat(measurementKind).isEqualTo(Aggregate.NOTAPPLICABLE);
    }

    @Test
    public void averageTest() {
        ObisCode obisCode1 = ObisCode.fromString("1.0.1.4.0.255");
        Aggregate measurementKind1 = AggregateMapping.getAggregateFor(obisCode1, macroPeriod);
        assertThat(measurementKind1).isEqualTo(Aggregate.AVERAGE);
        ObisCode obisCode2 = ObisCode.fromString("1.0.1.5.0.255");
        Aggregate measurementKind2 = AggregateMapping.getAggregateFor(obisCode2, macroPeriod);
        assertThat(measurementKind2).isEqualTo(Aggregate.AVERAGE);
        ObisCode obisCode3 = ObisCode.fromString("1.0.1.14.0.255");
        Aggregate measurementKind3 = AggregateMapping.getAggregateFor(obisCode3, macroPeriod);
        assertThat(measurementKind3).isEqualTo(Aggregate.AVERAGE);
        ObisCode obisCode4 = ObisCode.fromString("1.0.1.15.0.255");
        Aggregate measurementKind4 = AggregateMapping.getAggregateFor(obisCode4, macroPeriod);
        assertThat(measurementKind4).isEqualTo(Aggregate.AVERAGE);
        ObisCode obisCode5 = ObisCode.fromString("1.0.1.24.0.255");
        Aggregate measurementKind5 = AggregateMapping.getAggregateFor(obisCode5, macroPeriod);
        assertThat(measurementKind5).isEqualTo(Aggregate.AVERAGE);
        ObisCode obisCode6 = ObisCode.fromString("1.0.1.25.0.255");
        Aggregate measurementKind6 = AggregateMapping.getAggregateFor(obisCode6, macroPeriod);
        assertThat(measurementKind6).isEqualTo(Aggregate.AVERAGE);
        ObisCode obisCode7 = ObisCode.fromString("1.0.1.27.0.255");
        Aggregate measurementKind7 = AggregateMapping.getAggregateFor(obisCode7, macroPeriod);
        assertThat(measurementKind7).isEqualTo(Aggregate.AVERAGE);
        ObisCode obisCode8 = ObisCode.fromString("1.0.1.28.0.255");
        Aggregate measurementKind8 = AggregateMapping.getAggregateFor(obisCode8, macroPeriod);
        assertThat(measurementKind8).isEqualTo(Aggregate.AVERAGE);
    }

    @Test
    public void certainlyNotAnAverageTest() {
        ObisCode obisCode1 = ObisCode.fromString("1.0.1.6.0.255");
        Aggregate measurementKind1 = AggregateMapping.getAggregateFor(obisCode1, macroPeriod);
        assertThat(measurementKind1).isNotEqualTo(Aggregate.AVERAGE);
        ObisCode obisCode2 = ObisCode.fromString("1.0.0.5.0.255");
        Aggregate measurementKind2 = AggregateMapping.getAggregateFor(obisCode2, macroPeriod);
        assertThat(measurementKind2).isNotEqualTo(Aggregate.AVERAGE);
        ObisCode obisCode3 = ObisCode.fromString("2.0.1.14.0.255");
        Aggregate measurementKind3 = AggregateMapping.getAggregateFor(obisCode3, macroPeriod);
        assertThat(measurementKind3).isNotEqualTo(Aggregate.AVERAGE);
        ObisCode obisCode4 = ObisCode.fromString("1.0.1.30.0.255");
        Aggregate measurementKind4 = AggregateMapping.getAggregateFor(obisCode4, macroPeriod);
        assertThat(measurementKind4).isNotEqualTo(Aggregate.AVERAGE);
        ObisCode obisCode5 = ObisCode.fromString("1.0.94.24.0.255");
        Aggregate measurementKind5 = AggregateMapping.getAggregateFor(obisCode5, macroPeriod);
        assertThat(measurementKind5).isNotEqualTo(Aggregate.AVERAGE);
    }

    @Test
    public void excessTest() {
        ObisCode obisCode1 = ObisCode.fromString("1.0.1.34.0.255");
        Aggregate measurementKind1 = AggregateMapping.getAggregateFor(obisCode1, macroPeriod);
        assertThat(measurementKind1).isEqualTo(Aggregate.EXCESS);
        ObisCode obisCode2 = ObisCode.fromString("1.0.1.38.0.255");
        Aggregate measurementKind2 = AggregateMapping.getAggregateFor(obisCode2, macroPeriod);
        assertThat(measurementKind2).isEqualTo(Aggregate.EXCESS);
    }

    @Test
    public void certainlyNotExcessTest() {
        ObisCode obisCode1 = ObisCode.fromString("1.0.1.35.0.255");
        Aggregate measurementKind1 = AggregateMapping.getAggregateFor(obisCode1, macroPeriod);
        assertThat(measurementKind1).isNotEqualTo(Aggregate.EXCESS);
        ObisCode obisCode2 = ObisCode.fromString("1.0.0.34.0.255");
        Aggregate measurementKind2 = AggregateMapping.getAggregateFor(obisCode2, macroPeriod);
        assertThat(measurementKind2).isNotEqualTo(Aggregate.EXCESS);
        ObisCode obisCode3 = ObisCode.fromString("2.0.1.38.0.255");
        Aggregate measurementKind3 = AggregateMapping.getAggregateFor(obisCode3, macroPeriod);
        assertThat(measurementKind3).isNotEqualTo(Aggregate.EXCESS);
        ObisCode obisCode4 = ObisCode.fromString("1.0.1.8.0.255");
        Aggregate measurementKind4 = AggregateMapping.getAggregateFor(obisCode4, macroPeriod);
        assertThat(measurementKind4).isNotEqualTo(Aggregate.EXCESS);
    }

    @Test
    public void highThresholdTest() {
        ObisCode obisCode1 = ObisCode.fromString("1.0.1.35.0.255");
        Aggregate measurementKind1 = AggregateMapping.getAggregateFor(obisCode1, macroPeriod);
        assertThat(measurementKind1).isEqualTo(Aggregate.HIGHTHRESHOLD);
    }

    @Test
    public void certainlyNotAHighThresholdTest() {
        ObisCode obisCode1 = ObisCode.fromString("2.0.1.35.0.255");
        Aggregate measurementKind1 = AggregateMapping.getAggregateFor(obisCode1, macroPeriod);
        assertThat(measurementKind1).isNotEqualTo(Aggregate.HIGHTHRESHOLD);
        ObisCode obisCode2 = ObisCode.fromString("7.0.0.35.0.255");
        Aggregate measurementKind2 = AggregateMapping.getAggregateFor(obisCode2, macroPeriod);
        assertThat(measurementKind2).isNotEqualTo(Aggregate.HIGHTHRESHOLD);
        ObisCode obisCode3 = ObisCode.fromString("1.0.1.14.0.255");
        Aggregate measurementKind3 = AggregateMapping.getAggregateFor(obisCode3, macroPeriod);
        assertThat(measurementKind3).isNotEqualTo(Aggregate.HIGHTHRESHOLD);
        ObisCode obisCode4 = ObisCode.fromString("1.0.1.30.0.255");
        Aggregate measurementKind4 = AggregateMapping.getAggregateFor(obisCode4, macroPeriod);
        assertThat(measurementKind4).isNotEqualTo(Aggregate.HIGHTHRESHOLD);
    }

    @Test
    public void lowThresholdTest() {
        ObisCode obisCode1 = ObisCode.fromString("1.0.1.31.0.255");
        Aggregate measurementKind1 = AggregateMapping.getAggregateFor(obisCode1, macroPeriod);
        assertThat(measurementKind1).isEqualTo(Aggregate.LOWTHRESHOLD);
    }

    @Test
    public void certainlyNotALowThresholdTest() {
        ObisCode obisCode1 = ObisCode.fromString("2.0.1.31.0.255");
        Aggregate measurementKind1 = AggregateMapping.getAggregateFor(obisCode1, macroPeriod);
        assertThat(measurementKind1).isNotEqualTo(Aggregate.LOWTHRESHOLD);
        ObisCode obisCode2 = ObisCode.fromString("7.0.0.31.0.255");
        Aggregate measurementKind2 = AggregateMapping.getAggregateFor(obisCode2, macroPeriod);
        assertThat(measurementKind2).isNotEqualTo(Aggregate.LOWTHRESHOLD);
        ObisCode obisCode3 = ObisCode.fromString("1.0.1.14.0.255");
        Aggregate measurementKind3 = AggregateMapping.getAggregateFor(obisCode3, macroPeriod);
        assertThat(measurementKind3).isNotEqualTo(Aggregate.LOWTHRESHOLD);
        ObisCode obisCode4 = ObisCode.fromString("1.0.1.30.0.255");
        Aggregate measurementKind4 = AggregateMapping.getAggregateFor(obisCode4, macroPeriod);
        assertThat(measurementKind4).isNotEqualTo(Aggregate.LOWTHRESHOLD);
    }

    @Test
    public void maximumTest() {
        ObisCode obisCode1 = ObisCode.fromString("1.0.1.2.0.255");
        Aggregate measurementKind1 = AggregateMapping.getAggregateFor(obisCode1, macroPeriod);
        assertThat(measurementKind1).isEqualTo(Aggregate.MAXIMUM);
        ObisCode obisCode2 = ObisCode.fromString("1.0.1.6.0.255");
        Aggregate measurementKind2 = AggregateMapping.getAggregateFor(obisCode2, macroPeriod);
        assertThat(measurementKind2).isEqualTo(Aggregate.MAXIMUM);
        ObisCode obisCode3 = ObisCode.fromString("1.0.1.12.0.255");
        Aggregate measurementKind3 = AggregateMapping.getAggregateFor(obisCode3, macroPeriod);
        assertThat(measurementKind3).isEqualTo(Aggregate.MAXIMUM);
        ObisCode obisCode4 = ObisCode.fromString("1.0.1.16.0.255");
        Aggregate measurementKind4 = AggregateMapping.getAggregateFor(obisCode4, macroPeriod);
        assertThat(measurementKind4).isEqualTo(Aggregate.MAXIMUM);
        ObisCode obisCode5 = ObisCode.fromString("1.0.1.22.0.255");
        Aggregate measurementKind5 = AggregateMapping.getAggregateFor(obisCode5, macroPeriod);
        assertThat(measurementKind5).isEqualTo(Aggregate.MAXIMUM);
        ObisCode obisCode6 = ObisCode.fromString("1.0.1.26.0.255");
        Aggregate measurementKind6 = AggregateMapping.getAggregateFor(obisCode6, macroPeriod);
        assertThat(measurementKind6).isEqualTo(Aggregate.MAXIMUM);
        ObisCode obisCode7 = ObisCode.fromString("1.0.1.53.0.255");
        Aggregate measurementKind7 = AggregateMapping.getAggregateFor(obisCode7, macroPeriod);
        assertThat(measurementKind7).isEqualTo(Aggregate.MAXIMUM);
        ObisCode obisCode8 = ObisCode.fromString("1.0.1.54.0.255");
        Aggregate measurementKind8 = AggregateMapping.getAggregateFor(obisCode8, macroPeriod);
        assertThat(measurementKind8).isEqualTo(Aggregate.MAXIMUM);
    }

    @Test
    public void certainlyNotMaximumTest() {
        ObisCode obisCode1 = ObisCode.fromString("1.0.1.5.0.255");
        Aggregate measurementKind1 = AggregateMapping.getAggregateFor(obisCode1, macroPeriod);
        assertThat(measurementKind1).isNotEqualTo(Aggregate.MAXIMUM);
        ObisCode obisCode2 = ObisCode.fromString("1.0.0.6.0.255");
        Aggregate measurementKind2 = AggregateMapping.getAggregateFor(obisCode2, macroPeriod);
        assertThat(measurementKind2).isNotEqualTo(Aggregate.MAXIMUM);
        ObisCode obisCode3 = ObisCode.fromString("2.0.1.16.0.255");
        Aggregate measurementKind3 = AggregateMapping.getAggregateFor(obisCode3, macroPeriod);
        assertThat(measurementKind3).isNotEqualTo(Aggregate.MAXIMUM);
        ObisCode obisCode4 = ObisCode.fromString("1.0.1.30.0.255");
        Aggregate measurementKind4 = AggregateMapping.getAggregateFor(obisCode4, macroPeriod);
        assertThat(measurementKind4).isNotEqualTo(Aggregate.MAXIMUM);
        ObisCode obisCode5 = ObisCode.fromString("1.0.94.26.0.255");
        Aggregate measurementKind5 = AggregateMapping.getAggregateFor(obisCode5, macroPeriod);
        assertThat(measurementKind5).isNotEqualTo(Aggregate.MAXIMUM);
    }

    @Test
    public void minimumTest() {
        ObisCode obisCode1 = ObisCode.fromString("1.0.1.1.0.255");
        Aggregate measurementKind1 = AggregateMapping.getAggregateFor(obisCode1, macroPeriod);
        assertThat(measurementKind1).isEqualTo(Aggregate.MINIMUM);
        ObisCode obisCode2 = ObisCode.fromString("1.0.1.3.0.255");
        Aggregate measurementKind2 = AggregateMapping.getAggregateFor(obisCode2, macroPeriod);
        assertThat(measurementKind2).isEqualTo(Aggregate.MINIMUM);
        ObisCode obisCode3 = ObisCode.fromString("1.0.1.11.0.255");
        Aggregate measurementKind3 = AggregateMapping.getAggregateFor(obisCode3, macroPeriod);
        assertThat(measurementKind3).isEqualTo(Aggregate.MINIMUM);
        ObisCode obisCode4 = ObisCode.fromString("1.0.1.13.0.255");
        Aggregate measurementKind4 = AggregateMapping.getAggregateFor(obisCode4, macroPeriod);
        assertThat(measurementKind4).isEqualTo(Aggregate.MINIMUM);
        ObisCode obisCode5 = ObisCode.fromString("1.0.1.21.0.255");
        Aggregate measurementKind5 = AggregateMapping.getAggregateFor(obisCode5, macroPeriod);
        assertThat(measurementKind5).isEqualTo(Aggregate.MINIMUM);
        ObisCode obisCode6 = ObisCode.fromString("1.0.1.23.0.255");
        Aggregate measurementKind6 = AggregateMapping.getAggregateFor(obisCode6, macroPeriod);
        assertThat(measurementKind6).isEqualTo(Aggregate.MINIMUM);
        ObisCode obisCode7 = ObisCode.fromString("1.0.1.51.0.255");
        Aggregate measurementKind7 = AggregateMapping.getAggregateFor(obisCode7, macroPeriod);
        assertThat(measurementKind7).isEqualTo(Aggregate.MINIMUM);
        ObisCode obisCode8 = ObisCode.fromString("1.0.1.52.0.255");
        Aggregate measurementKind8 = AggregateMapping.getAggregateFor(obisCode8, macroPeriod);
        assertThat(measurementKind8).isEqualTo(Aggregate.MINIMUM);
    }

    @Test
    public void certainlyNotAMinimumTest() {
        ObisCode obisCode1 = ObisCode.fromString("1.0.1.5.0.255");
        Aggregate measurementKind1 = AggregateMapping.getAggregateFor(obisCode1, macroPeriod);
        assertThat(measurementKind1).isNotEqualTo(Aggregate.MINIMUM);
        ObisCode obisCode2 = ObisCode.fromString("1.0.0.3.0.255");
        Aggregate measurementKind2 = AggregateMapping.getAggregateFor(obisCode2, macroPeriod);
        assertThat(measurementKind2).isNotEqualTo(Aggregate.MINIMUM);
        ObisCode obisCode3 = ObisCode.fromString("2.0.1.13.0.255");
        Aggregate measurementKind3 = AggregateMapping.getAggregateFor(obisCode3, macroPeriod);
        assertThat(measurementKind3).isNotEqualTo(Aggregate.MINIMUM);
        ObisCode obisCode4 = ObisCode.fromString("1.0.1.30.0.255");
        Aggregate measurementKind4 = AggregateMapping.getAggregateFor(obisCode4, macroPeriod);
        assertThat(measurementKind4).isNotEqualTo(Aggregate.MINIMUM);
        ObisCode obisCode5 = ObisCode.fromString("1.0.94.51.0.255");
        Aggregate measurementKind5 = AggregateMapping.getAggregateFor(obisCode5, macroPeriod);
        assertThat(measurementKind5).isNotEqualTo(Aggregate.MINIMUM);
    }
}
