package com.elster.jupiter.metering.impl;

import java.util.Currency;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Aggregate;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.google.common.collect.ImmutableList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ReadingTypeCodeBuilderTest {

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testForwardEnergy15m() {
    	String code = ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).
    		measure(MeasurementKind.ENERGY).in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR).flow(FlowDirection.FORWARD).
    		period(TimeAttribute.MINUTE15).accumulate(Accumulation.DELTADELTA).code();
        assertThat(code).isEqualTo("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0");
    }
    
    @Test
    public void testReverseEnergy1h() {
    	String code =ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).
    		measure(MeasurementKind.ENERGY).in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR).flow(FlowDirection.REVERSE).
    		period(TimeAttribute.MINUTE60).accumulate(Accumulation.DELTADELTA).code();
         assertThat(code).isEqualTo("0.0.7.4.19.1.12.0.0.0.0.0.0.0.0.3.72.0");
    }
    
    @Test
    public void testForwardEnergy() {
    	String code = ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).
        	measure(MeasurementKind.ENERGY).in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR).flow(FlowDirection.FORWARD).
        	accumulate(Accumulation.BULKQUANTITY).code();
        assertThat(code).isEqualTo("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0");
    }
    
    @Test
    public void testForwardPower30m() {
    	String code = ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).
    		measure(MeasurementKind.POWER).in(MetricMultiplier.ZERO, ReadingTypeUnit.WATT).flow(FlowDirection.FORWARD).
    		period(TimeAttribute.MINUTE30).accumulate(Accumulation.INDICATING).code();
        assertThat(code).isEqualTo("0.0.5.6.1.1.37.0.0.0.0.0.0.0.0.0.38.0");
    }
    
    @Test
    public void testLaggingReactiveEnergy10m() {
    	String code = ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).
        		measure(MeasurementKind.ENERGY).in(MetricMultiplier.KILO, ReadingTypeUnit.VOLTAMPEREREACTIVEHOUR).flow(FlowDirection.LAGGING).
        		period(TimeAttribute.MINUTE10).accumulate(Accumulation.DELTADELTA).code();
        assertThat(code).isEqualTo("0.0.1.4.2.1.12.0.0.0.0.0.0.0.0.3.73.0");
    }
    
    @Test
    public void testDailyOffPeakForwardEnergy() {
    	String code = ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED).
            	measure(MeasurementKind.ENERGY).in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR).flow(FlowDirection.FORWARD).
            	period(TimeAttribute.HOUR24).accumulate(Accumulation.SUMMATION).tou(2).code();
    	assertThat(code).isEqualTo("0.0.4.9.1.1.12.0.0.0.0.2.0.0.0.3.72.0");
    }
}
