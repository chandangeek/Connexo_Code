/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cbo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;

import static com.elster.jupiter.cbo.Accumulation.*;
import static com.elster.jupiter.cbo.Commodity.*;
import static com.elster.jupiter.cbo.FlowDirection.*;
import static com.elster.jupiter.cbo.MeasurementKind.*;
import static com.elster.jupiter.cbo.MetricMultiplier.*;
import static com.elster.jupiter.cbo.ReadingTypeUnit.*;
import static com.elster.jupiter.cbo.TimeAttribute.*;

public class ReadingTypeCodeBuilderTest {

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testForwardEnergy15m() {
    	String code = ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).
    		measure(ENERGY).in(KILO, WATTHOUR).flow(FORWARD).period(MINUTE15).accumulate(DELTADELTA).code();
        assertThat(code).isEqualTo("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0");
    }
    
    @Test
    public void testReverseEnergy1h() {
    	String code =ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).
    		measure(ENERGY).in(KILO, WATTHOUR).flow(REVERSE).period(MINUTE60).accumulate(DELTADELTA).code();
         assertThat(code).isEqualTo("0.0.7.4.19.1.12.0.0.0.0.0.0.0.0.3.72.0");
    }
    
    @Test
    public void testForwardEnergy() {
    	String code = ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).
    		measure(ENERGY).in(KILO, WATTHOUR).flow(FORWARD).accumulate(BULKQUANTITY).code();
        assertThat(code).isEqualTo("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0");
    }
    
    @Test
    public void testForwardPower30m() {
    	String code = ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).
    		measure(POWER).in(WATT).flow(FORWARD).period(MINUTE30).accumulate(INDICATING).code();
        assertThat(code).isEqualTo("0.0.5.6.1.1.37.0.0.0.0.0.0.0.0.0.38.0");
    }
    
    @Test
    public void testLaggingReactiveEnergy10m() {
    	String code = ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).
        	measure(ENERGY).in(KILO, VOLTAMPEREREACTIVEHOUR).flow(LAGGING).period(MINUTE10).accumulate(DELTADELTA).code();
        assertThat(code).isEqualTo("0.0.1.4.2.1.12.0.0.0.0.0.0.0.0.3.73.0");
    }
    
    @Test
    public void testDailyOffPeakForwardEnergy() {
    	String code = ReadingTypeCodeBuilder.of(ELECTRICITY_SECONDARY_METERED).
            	measure(ENERGY).in(KILO, WATTHOUR).flow(FORWARD).
            	period(HOUR24).accumulate(SUMMATION).tou(2).code();
    	assertThat(code).isEqualTo("0.0.4.9.1.1.12.0.0.0.0.2.0.0.0.3.72.0");
    }
}
