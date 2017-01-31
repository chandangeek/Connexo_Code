/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.metering.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.ObisCode;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class AccumulationMappingTest {

    private final TimeDuration timeDuration = TimeDuration.hours(1);

    @Test
    public void nullSafeObisCodeTest() {
        ObisCode nullSafe = null;
        Accumulation measurementKind = AccumulationMapping.getAccumulationFor(nullSafe, timeDuration);
        assertThat(measurementKind).isEqualTo(Accumulation.NOTAPPLICABLE);
    }

    @Test
    public void bulkQuantityTest() {
        ObisCode obisCode1 = ObisCode.fromString("1.0.1.8.0.255");
        Accumulation measurementKind1 = AccumulationMapping.getAccumulationFor(obisCode1, timeDuration);
        assertThat(measurementKind1).isEqualTo(Accumulation.BULKQUANTITY);
        ObisCode obisCode2 = ObisCode.fromString("1.0.20.8.0.255");
        Accumulation measurementKind2 = AccumulationMapping.getAccumulationFor(obisCode2, timeDuration);
        assertThat(measurementKind2).isEqualTo(Accumulation.BULKQUANTITY);
        ObisCode obisCode5 = ObisCode.fromString("1.0.7.8.0.255");
        Accumulation measurementKind5 = AccumulationMapping.getAccumulationFor(obisCode5, timeDuration);
        assertThat(measurementKind5).isEqualTo(Accumulation.BULKQUANTITY);
    }

    @Test
    public void certainlyNotBulkQuantityTest() {
        ObisCode obisCode1 = ObisCode.fromString("1.0.1.8.100.255");
        Accumulation measurementKind1 = AccumulationMapping.getAccumulationFor(obisCode1, timeDuration);
        assertThat(measurementKind1).isNotEqualTo(Accumulation.BULKQUANTITY);
        ObisCode obisCode2 = ObisCode.fromString("1.0.0.8.0.255");
        Accumulation measurementKind2 = AccumulationMapping.getAccumulationFor(obisCode2, timeDuration);
        assertThat(measurementKind2).isNotEqualTo(Accumulation.BULKQUANTITY);
        ObisCode obisCode3 = ObisCode.fromString("1.0.1.7.0.255");
        Accumulation measurementKind3 = AccumulationMapping.getAccumulationFor(obisCode3, timeDuration);
        assertThat(measurementKind3).isNotEqualTo(Accumulation.BULKQUANTITY);
        ObisCode obisCode4 = ObisCode.fromString("1.0.3.39.0.255");
        Accumulation measurementKind4 = AccumulationMapping.getAccumulationFor(obisCode4, timeDuration);
        assertThat(measurementKind4).isNotEqualTo(Accumulation.BULKQUANTITY);
        ObisCode obisCode5 = ObisCode.fromString("1.0.32.23.1.255");
        Accumulation measurementKind5 = AccumulationMapping.getAccumulationFor(obisCode5, timeDuration);
        assertThat(measurementKind5).isNotEqualTo(Accumulation.BULKQUANTITY);
        ObisCode obisCode6 = ObisCode.fromString("7.0.32.23.1.255");
        Accumulation measurementKind6 = AccumulationMapping.getAccumulationFor(obisCode6, timeDuration);
        assertThat(measurementKind6).isNotEqualTo(Accumulation.BULKQUANTITY);
    }

    @Test
    public void indicatingTest() {
        ObisCode obisCode1 = ObisCode.fromString("1.0.1.7.0.255");
        Accumulation measurementKind1 = AccumulationMapping.getAccumulationFor(obisCode1, timeDuration);
        assertThat(measurementKind1).isEqualTo(Accumulation.INDICATING);
        ObisCode obisCode2 = ObisCode.fromString("1.0.20.7.0.255");
        Accumulation measurementKind2 = AccumulationMapping.getAccumulationFor(obisCode2, timeDuration);
        assertThat(measurementKind2).isEqualTo(Accumulation.INDICATING);
        ObisCode obisCode3 = ObisCode.fromString("1.0.1.24.1.255");
        Accumulation measurementKind3 = AccumulationMapping.getAccumulationFor(obisCode3, timeDuration);
        assertThat(measurementKind3).isEqualTo(Accumulation.INDICATING);
        ObisCode obisCode4 = ObisCode.fromString("1.0.3.7.4.255");
        Accumulation measurementKind4 = AccumulationMapping.getAccumulationFor(obisCode4, timeDuration);
        assertThat(measurementKind4).isEqualTo(Accumulation.INDICATING);
        ObisCode obisCode5 = ObisCode.fromString("1.0.6.7.8.255");
        Accumulation measurementKind5 = AccumulationMapping.getAccumulationFor(obisCode5, timeDuration);
        assertThat(measurementKind5).isEqualTo(Accumulation.INDICATING);
        ObisCode obisCode6 = ObisCode.fromString("1.0.1.4.8.255");
        Accumulation measurementKind6 = AccumulationMapping.getAccumulationFor(obisCode6, timeDuration);
        assertThat(measurementKind6).isEqualTo(Accumulation.INDICATING);
        ObisCode obisCode7 = ObisCode.fromString("1.0.1.3.8.255");
        Accumulation measurementKind7 = AccumulationMapping.getAccumulationFor(obisCode7, timeDuration);
        assertThat(measurementKind7).isEqualTo(Accumulation.INDICATING);
        ObisCode obisCode8 = ObisCode.fromString("1.0.1.14.8.255");
        Accumulation measurementKind8 = AccumulationMapping.getAccumulationFor(obisCode8, timeDuration);
        assertThat(measurementKind8).isEqualTo(Accumulation.INDICATING);
        ObisCode obisCode9 = ObisCode.fromString("1.0.1.15.8.255");
        Accumulation measurementKind9 = AccumulationMapping.getAccumulationFor(obisCode9, timeDuration);
        assertThat(measurementKind9).isEqualTo(Accumulation.INDICATING);
        ObisCode obisCode10 = ObisCode.fromString("1.0.1.24.8.255");
        Accumulation measurementKind10 = AccumulationMapping.getAccumulationFor(obisCode10, timeDuration);
        assertThat(measurementKind10).isEqualTo(Accumulation.INDICATING);
        ObisCode obisCode11 = ObisCode.fromString("1.0.1.25.8.255");
        Accumulation measurementKind11 = AccumulationMapping.getAccumulationFor(obisCode11, timeDuration);
        assertThat(measurementKind11).isEqualTo(Accumulation.INDICATING);
        ObisCode obisCode12 = ObisCode.fromString("1.0.1.27.8.255");
        Accumulation measurementKind12 = AccumulationMapping.getAccumulationFor(obisCode12, timeDuration);
        assertThat(measurementKind12).isEqualTo(Accumulation.INDICATING);
        ObisCode obisCode13 = ObisCode.fromString("1.0.1.28.8.255");
        Accumulation measurementKind13 = AccumulationMapping.getAccumulationFor(obisCode13, timeDuration);
        assertThat(measurementKind13).isEqualTo(Accumulation.INDICATING);
    }

    @Test
    public void certainlyNotIndicatingTest() {
        ObisCode obisCode1 = ObisCode.fromString("1.0.1.8.0.255");
        Accumulation measurementKind1 = AccumulationMapping.getAccumulationFor(obisCode1, timeDuration);
        assertThat(measurementKind1).isNotEqualTo(Accumulation.INDICATING);
        ObisCode obisCode2 = ObisCode.fromString("1.0.0.7.0.255");
        Accumulation measurementKind2 = AccumulationMapping.getAccumulationFor(obisCode2, timeDuration);
        assertThat(measurementKind2).isNotEqualTo(Accumulation.INDICATING);
        ObisCode obisCode3 = ObisCode.fromString("1.0.94.7.1.255");
        Accumulation measurementKind3 = AccumulationMapping.getAccumulationFor(obisCode3, timeDuration);
        assertThat(measurementKind3).isNotEqualTo(Accumulation.INDICATING);
        ObisCode obisCode4 = ObisCode.fromString("7.0.3.7.4.255");
        Accumulation measurementKind4 = AccumulationMapping.getAccumulationFor(obisCode4, timeDuration);
        assertThat(measurementKind4).isNotEqualTo(Accumulation.INDICATING);
        ObisCode obisCode5 = ObisCode.fromString("1.0.1.8.8.255");
        Accumulation measurementKind5 = AccumulationMapping.getAccumulationFor(obisCode5, timeDuration);
        assertThat(measurementKind5).isNotEqualTo(Accumulation.INDICATING);
    }

    @Test
    public void summationTest() {
        TimeDuration dailyDuration = TimeDuration.days(1);
        ObisCode obisCode1 = ObisCode.fromString("1.0.1.8.1.255");
        Accumulation measurementKind1 = AccumulationMapping.getAccumulationFor(obisCode1, dailyDuration);
        assertThat(measurementKind1).isEqualTo(Accumulation.SUMMATION);
        ObisCode obisCode2 = ObisCode.fromString("1.0.20.8.2.255");
        Accumulation measurementKind2 = AccumulationMapping.getAccumulationFor(obisCode2, dailyDuration);
        assertThat(measurementKind2).isEqualTo(Accumulation.SUMMATION);
        ObisCode obisCode3 = ObisCode.fromString("1.0.1.8.3.255");
        Accumulation measurementKind3 = AccumulationMapping.getAccumulationFor(obisCode3, dailyDuration);
        assertThat(measurementKind3).isEqualTo(Accumulation.SUMMATION);
        ObisCode obisCode4 = ObisCode.fromString("1.0.1.8.50.255");
        Accumulation measurementKind4 = AccumulationMapping.getAccumulationFor(obisCode4, dailyDuration);
        assertThat(measurementKind4).isEqualTo(Accumulation.SUMMATION);
        ObisCode obisCode5 = ObisCode.fromString("1.0.7.8.63.255");
        Accumulation measurementKind5 = AccumulationMapping.getAccumulationFor(obisCode5, dailyDuration);
        assertThat(measurementKind5).isEqualTo(Accumulation.SUMMATION);
    }

    @Test
    public void certainlyNotSummationTest() {
        ObisCode obisCode1 = ObisCode.fromString("1.0.1.8.1.255");
        Accumulation measurementKind1 = AccumulationMapping.getAccumulationFor(obisCode1, timeDuration);
        assertThat(measurementKind1).isNotEqualTo(Accumulation.SUMMATION);
        ObisCode obisCode2 = ObisCode.fromString("1.0.20.8.2.255");
        Accumulation measurementKind2 = AccumulationMapping.getAccumulationFor(obisCode2, timeDuration);
        assertThat(measurementKind2).isNotEqualTo(Accumulation.SUMMATION);
        ObisCode obisCode3 = ObisCode.fromString("1.0.93.10.3.255");
        Accumulation measurementKind3 = AccumulationMapping.getAccumulationFor(obisCode3, timeDuration);
        assertThat(measurementKind3).isNotEqualTo(Accumulation.SUMMATION);
        ObisCode obisCode4 = ObisCode.fromString("7.0.1.29.50.255");
        Accumulation measurementKind4 = AccumulationMapping.getAccumulationFor(obisCode4, timeDuration);
        assertThat(measurementKind4).isNotEqualTo(Accumulation.SUMMATION);
        ObisCode obisCode5 = ObisCode.fromString("1.0.7.8.80.255");
        Accumulation measurementKind5 = AccumulationMapping.getAccumulationFor(obisCode5, timeDuration);
        assertThat(measurementKind5).isNotEqualTo(Accumulation.SUMMATION);
    }

    @Test
    public void cumulativeTest() {
        ObisCode obisCode1 = ObisCode.fromString("1.0.1.1.0.255");
        Accumulation measurementKind1 = AccumulationMapping.getAccumulationFor(obisCode1, timeDuration);
        assertThat(measurementKind1).isEqualTo(Accumulation.CUMULATIVE);
        ObisCode obisCode2 = ObisCode.fromString("1.0.20.2.2.255");
        Accumulation measurementKind2 = AccumulationMapping.getAccumulationFor(obisCode2, timeDuration);
        assertThat(measurementKind2).isEqualTo(Accumulation.CUMULATIVE);
        ObisCode obisCode3 = ObisCode.fromString("1.0.1.11.0.255");
        Accumulation measurementKind3 = AccumulationMapping.getAccumulationFor(obisCode3, timeDuration);
        assertThat(measurementKind3).isEqualTo(Accumulation.CUMULATIVE);
        ObisCode obisCode4 = ObisCode.fromString("1.0.1.12.0.255");
        Accumulation measurementKind4 = AccumulationMapping.getAccumulationFor(obisCode4, timeDuration);
        assertThat(measurementKind4).isEqualTo(Accumulation.CUMULATIVE);
        ObisCode obisCode5 = ObisCode.fromString("1.0.7.21.63.255");
        Accumulation measurementKind5 = AccumulationMapping.getAccumulationFor(obisCode5, timeDuration);
        assertThat(measurementKind5).isEqualTo(Accumulation.CUMULATIVE);
    }

    @Test
    public void certainlyNotCumulativeTest() {
        ObisCode obisCode1 = ObisCode.fromString("1.0.1.8.0.255");
        Accumulation measurementKind1 = AccumulationMapping.getAccumulationFor(obisCode1, timeDuration);
        assertThat(measurementKind1).isNotEqualTo(Accumulation.CUMULATIVE);
        ObisCode obisCode2 = ObisCode.fromString("1.0.20.13.2.255");
        Accumulation measurementKind2 = AccumulationMapping.getAccumulationFor(obisCode2, timeDuration);
        assertThat(measurementKind2).isNotEqualTo(Accumulation.CUMULATIVE);
        ObisCode obisCode3 = ObisCode.fromString("1.0.93.10.3.255");
        Accumulation measurementKind3 = AccumulationMapping.getAccumulationFor(obisCode3, timeDuration);
        assertThat(measurementKind3).isNotEqualTo(Accumulation.CUMULATIVE);
        ObisCode obisCode4 = ObisCode.fromString("7.0.1.29.50.255");
        Accumulation measurementKind4 = AccumulationMapping.getAccumulationFor(obisCode4, timeDuration);
        assertThat(measurementKind4).isNotEqualTo(Accumulation.CUMULATIVE);
        ObisCode obisCode5 = ObisCode.fromString("1.0.7.8.80.255");
        Accumulation measurementKind5 = AccumulationMapping.getAccumulationFor(obisCode5, timeDuration);
        assertThat(measurementKind5).isNotEqualTo(Accumulation.CUMULATIVE);
    }

    @Test
    public void deltaDataTest() {
        ObisCode obisCode1 = ObisCode.fromString("1.0.1.5.1.255");
        Accumulation measurementKind1 = AccumulationMapping.getAccumulationFor(obisCode1, timeDuration);
        assertThat(measurementKind1).isEqualTo(Accumulation.DELTADELTA);
        ObisCode obisCode2 = ObisCode.fromString("1.0.20.6.2.255");
        Accumulation measurementKind2 = AccumulationMapping.getAccumulationFor(obisCode2, timeDuration);
        assertThat(measurementKind2).isEqualTo(Accumulation.DELTADELTA);
        ObisCode obisCode3 = ObisCode.fromString("1.0.1.5.0.255");
        Accumulation measurementKind3 = AccumulationMapping.getAccumulationFor(obisCode3, timeDuration);
        assertThat(measurementKind3).isEqualTo(Accumulation.DELTADELTA);
        ObisCode obisCode4 = ObisCode.fromString("1.0.1.6.0.255");
        Accumulation measurementKind4 = AccumulationMapping.getAccumulationFor(obisCode4, timeDuration);
        assertThat(measurementKind4).isEqualTo(Accumulation.DELTADELTA);
    }

    @Test
    public void notDeltaDataTest() {
        ObisCode obisCode1 = ObisCode.fromString("1.0.1.8.0.255");
        Accumulation measurementKind1 = AccumulationMapping.getAccumulationFor(obisCode1, timeDuration);
        assertThat(measurementKind1).isNotEqualTo(Accumulation.DELTADELTA);
        ObisCode obisCode2 = ObisCode.fromString("1.0.20.2.2.255");
        Accumulation measurementKind2 = AccumulationMapping.getAccumulationFor(obisCode2, timeDuration);
        assertThat(measurementKind2).isNotEqualTo(Accumulation.DELTADELTA);
        ObisCode obisCode3 = ObisCode.fromString("1.0.1.11.0.255");
        Accumulation measurementKind3 = AccumulationMapping.getAccumulationFor(obisCode3, timeDuration);
        assertThat(measurementKind3).isNotEqualTo(Accumulation.DELTADELTA);
        ObisCode obisCode4 = ObisCode.fromString("1.0.1.12.0.255");
        Accumulation measurementKind4 = AccumulationMapping.getAccumulationFor(obisCode4, timeDuration);
        assertThat(measurementKind4).isNotEqualTo(Accumulation.DELTADELTA);
    }
}
