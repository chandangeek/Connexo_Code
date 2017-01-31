/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.metering.impl;

import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.ObisCode;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class MeasuringPeriodMappingTest {

    private final ObisCode obisCode = ObisCode.fromString("1.0.1.8.0.255");

    @Test
    public void nullSafeObisCodeTest() {
        ObisCode nullSafe = null;
        TimeAttribute timeAttribute = MeasuringPeriodMapping.getMeasuringPeriodFor(nullSafe, new TimeDuration(1));

        assertThat(timeAttribute).isEqualTo(TimeAttribute.NOTAPPLICABLE);
    }

    @Test
    public void nullSafeTimeDurationTest() {
        TimeDuration nullSafe = null;
        TimeAttribute timeAttribute = MeasuringPeriodMapping.getMeasuringPeriodFor(obisCode, nullSafe);

        assertThat(timeAttribute).isEqualTo(TimeAttribute.NOTAPPLICABLE);
    }

    @Test
    public void oneMinuteTest() {
        TimeAttribute timeAttribute = MeasuringPeriodMapping.getMeasuringPeriodFor(obisCode, new TimeDuration(60));
        assertThat(timeAttribute).isEqualTo(TimeAttribute.MINUTE1);
    }

    @Test
    public void twoMinuteTest() {
        TimeAttribute timeAttribute = MeasuringPeriodMapping.getMeasuringPeriodFor(obisCode, new TimeDuration(120));
        assertThat(timeAttribute).isEqualTo(TimeAttribute.MINUTE2);
    }

    @Test
    public void threeMinuteTest() {
        TimeAttribute timeAttribute = MeasuringPeriodMapping.getMeasuringPeriodFor(obisCode, new TimeDuration(180));
        assertThat(timeAttribute).isEqualTo(TimeAttribute.MINUTE3);
    }

    @Test
    public void fiveMinuteTest() {
        TimeAttribute timeAttribute = MeasuringPeriodMapping.getMeasuringPeriodFor(obisCode, new TimeDuration(300));
        assertThat(timeAttribute).isEqualTo(TimeAttribute.MINUTE5);
    }

    @Test
    public void tenMinuteTest() {
        TimeAttribute timeAttribute = MeasuringPeriodMapping.getMeasuringPeriodFor(obisCode, new TimeDuration(600));
        assertThat(timeAttribute).isEqualTo(TimeAttribute.MINUTE10);
    }

    @Test
    public void fifteenMinuteTest() {
        TimeAttribute timeAttribute = MeasuringPeriodMapping.getMeasuringPeriodFor(obisCode, new TimeDuration(900));
        assertThat(timeAttribute).isEqualTo(TimeAttribute.MINUTE15);
    }

    @Test
    public void twentyMinuteTest() {
        TimeAttribute timeAttribute = MeasuringPeriodMapping.getMeasuringPeriodFor(obisCode, new TimeDuration(1200));
        assertThat(timeAttribute).isEqualTo(TimeAttribute.MINUTE20);
    }

    @Test
    public void thirtyMinuteTest() {
        TimeAttribute timeAttribute = MeasuringPeriodMapping.getMeasuringPeriodFor(obisCode, new TimeDuration(1800));
        assertThat(timeAttribute).isEqualTo(TimeAttribute.MINUTE30);
    }

    @Test
    public void sixtyMinuteTest() {
        TimeAttribute timeAttribute = MeasuringPeriodMapping.getMeasuringPeriodFor(obisCode, new TimeDuration(3600));
        assertThat(timeAttribute).isEqualTo(TimeAttribute.MINUTE60);
    }
    @Test
    public void twentyFourHoursNoTOUTest() {
        TimeAttribute timeAttribute = MeasuringPeriodMapping.getMeasuringPeriodFor(obisCode, new TimeDuration(86400));
        assertThat(timeAttribute).isEqualTo(TimeAttribute.HOUR24);
    }
    @Test
    public void twentyFourHoursButTOUTest() {
        TimeAttribute timeAttribute = MeasuringPeriodMapping.getMeasuringPeriodFor(ObisCode.fromString("1.0.1.8.1.255"), new TimeDuration(86400));
        assertThat(timeAttribute).isEqualTo(TimeAttribute.NOTAPPLICABLE);
    }
}
