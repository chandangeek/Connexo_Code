/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.ReadingType;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link VirtualReadingType#isRegular()} method.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-03-01 (09:54)
 */
public class VirtualReadingTypeRegularTest {

    @Test
    public void fifteenMinsWattHourIsRegular() {
        ReadingType rt_15minWh = mock(ReadingType.class);
        when(rt_15minWh.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(rt_15minWh.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(rt_15minWh.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(rt_15minWh.getAccumulation()).thenReturn(Accumulation.DELTADELTA);
        when(rt_15minWh.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        VirtualReadingType fifteenMinsWh = VirtualReadingType.from(rt_15minWh);

        // Business method + asserts
        assertThat(fifteenMinsWh.isRegular()).isTrue();
    }

    @Test
    public void wattBulkIsNotRegular() {
        ReadingType rt_BulkWatt = mock(ReadingType.class);
        when(rt_BulkWatt.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(rt_BulkWatt.getMeasuringPeriod()).thenReturn(TimeAttribute.NOTAPPLICABLE);
        when(rt_BulkWatt.getUnit()).thenReturn(ReadingTypeUnit.WATT);
        when(rt_BulkWatt.getAccumulation()).thenReturn(Accumulation.BULKQUANTITY);
        when(rt_BulkWatt.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        VirtualReadingType bulkWatt = VirtualReadingType.from(rt_BulkWatt);

        // Business method + asserts
        assertThat(bulkWatt.isRegular()).isFalse();
    }

}