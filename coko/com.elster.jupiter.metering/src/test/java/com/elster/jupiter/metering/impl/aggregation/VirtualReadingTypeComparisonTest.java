/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

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
 * Tests the comparison aspect of the {@link VirtualReadingType} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-03-01 (09:54)
 */
public class VirtualReadingTypeComparisonTest {

    @Test
    public void kWh15MinIsSmallerThan_kWh60min() {
        ReadingType rt_15minkWh = mock(ReadingType.class);
        when(rt_15minkWh.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(rt_15minkWh.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(rt_15minkWh.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(rt_15minkWh.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        VirtualReadingType kWh15mins = VirtualReadingType.from(rt_15minkWh);

        ReadingType rt_60minkWh = mock(ReadingType.class);
        when(rt_60minkWh.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(rt_60minkWh.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE60);
        when(rt_60minkWh.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(rt_60minkWh.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        VirtualReadingType kWh60mins = VirtualReadingType.from(rt_60minkWh);

        // Business method + asserts
        assertThat(kWh15mins.compareTo(kWh60mins)).isLessThan(0);
    }

    @Test
    public void kWh60MinIsBiggerThan_kWh15min() {
        ReadingType rt_15minkWh = mock(ReadingType.class);
        when(rt_15minkWh.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(rt_15minkWh.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(rt_15minkWh.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(rt_15minkWh.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        VirtualReadingType kWh15mins = VirtualReadingType.from(rt_15minkWh);

        ReadingType rt_60minkWh = mock(ReadingType.class);
        when(rt_60minkWh.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(rt_60minkWh.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE60);
        when(rt_60minkWh.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(rt_60minkWh.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        VirtualReadingType kWh60mins = VirtualReadingType.from(rt_60minkWh);

        // Business method + asserts
        assertThat(kWh60mins.compareTo(kWh15mins)).isGreaterThan(0);
    }

    @Test
    public void kW15MinIsSmallerThan_kWh60min() {
        ReadingType rt_15minkW = mock(ReadingType.class);
        when(rt_15minkW.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(rt_15minkW.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(rt_15minkW.getUnit()).thenReturn(ReadingTypeUnit.WATT);
        when(rt_15minkW.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        VirtualReadingType kW15mins = VirtualReadingType.from(rt_15minkW);

        ReadingType rt_60minkWh = mock(ReadingType.class);
        when(rt_60minkWh.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(rt_60minkWh.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE60);
        when(rt_60minkWh.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(rt_60minkWh.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        VirtualReadingType kWh60mins = VirtualReadingType.from(rt_60minkWh);

        // Business method + asserts
        assertThat(kW15mins.compareTo(kWh60mins)).isLessThan(0);
    }

    @Test
    public void kWh60MinIsBiggerThan_kW15min() {
        ReadingType rt_15minkW = mock(ReadingType.class);
        when(rt_15minkW.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(rt_15minkW.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(rt_15minkW.getUnit()).thenReturn(ReadingTypeUnit.WATT);
        when(rt_15minkW.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        VirtualReadingType kW15mins = VirtualReadingType.from(rt_15minkW);

        ReadingType rt_60minkWh = mock(ReadingType.class);
        when(rt_60minkWh.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(rt_60minkWh.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE60);
        when(rt_60minkWh.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(rt_60minkWh.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        VirtualReadingType kWh60mins = VirtualReadingType.from(rt_60minkWh);

        // Business method + asserts
        assertThat(kWh60mins.compareTo(kW15mins)).isGreaterThan(0);
    }

    @Test
    public void kWh15MinIsSmallerThan_MWh15min() {
        ReadingType rt_15minMWh = mock(ReadingType.class);
        when(rt_15minMWh.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(rt_15minMWh.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(rt_15minMWh.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(rt_15minMWh.getMultiplier()).thenReturn(MetricMultiplier.MEGA);
        VirtualReadingType MWh15mins = VirtualReadingType.from(rt_15minMWh);

        ReadingType rt_15minkWh = mock(ReadingType.class);
        when(rt_15minkWh.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(rt_15minkWh.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(rt_15minkWh.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(rt_15minkWh.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        VirtualReadingType kWh15mins = VirtualReadingType.from(rt_15minkWh);

        // Business method + asserts
        assertThat(kWh15mins.compareTo(MWh15mins)).isLessThan(0);
    }

    @Test
    public void MWh15MinIsBiggerThan_kWh15min() {
        ReadingType rt_15minMWh = mock(ReadingType.class);
        when(rt_15minMWh.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(rt_15minMWh.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(rt_15minMWh.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(rt_15minMWh.getMultiplier()).thenReturn(MetricMultiplier.MEGA);
        VirtualReadingType MWh15mins = VirtualReadingType.from(rt_15minMWh);

        ReadingType rt_15minkWh = mock(ReadingType.class);
        when(rt_15minkWh.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(rt_15minkWh.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(rt_15minkWh.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(rt_15minkWh.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        VirtualReadingType kWh15mins = VirtualReadingType.from(rt_15minkWh);

        // Business method + asserts
        assertThat(MWh15mins.compareTo(kWh15mins)).isGreaterThan(0);
    }

    @Test
    public void meterIsSmallerThan_kiloMeter() {
        ReadingType rt_15minMeter = mock(ReadingType.class);
        when(rt_15minMeter.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(rt_15minMeter.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(rt_15minMeter.getUnit()).thenReturn(ReadingTypeUnit.METER);
        when(rt_15minMeter.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        VirtualReadingType m15mins = VirtualReadingType.from(rt_15minMeter);

        ReadingType rt_15minKm = mock(ReadingType.class);
        when(rt_15minKm.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(rt_15minKm.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(rt_15minKm.getUnit()).thenReturn(ReadingTypeUnit.METER);
        when(rt_15minKm.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        VirtualReadingType km15mins = VirtualReadingType.from(rt_15minKm);

        // Business method + asserts
        assertThat(m15mins.compareTo(km15mins)).isLessThan(0);
    }

    @Test
    public void kiloMeterIsBiggerThan_meter() {
        ReadingType rt_15minMeter = mock(ReadingType.class);
        when(rt_15minMeter.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(rt_15minMeter.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(rt_15minMeter.getUnit()).thenReturn(ReadingTypeUnit.METER);
        when(rt_15minMeter.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        VirtualReadingType m15mins = VirtualReadingType.from(rt_15minMeter);

        ReadingType rt_15minKm = mock(ReadingType.class);
        when(rt_15minKm.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(rt_15minKm.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(rt_15minKm.getUnit()).thenReturn(ReadingTypeUnit.METER);
        when(rt_15minKm.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        VirtualReadingType km15mins = VirtualReadingType.from(rt_15minKm);

        // Business method + asserts
        assertThat(km15mins.compareTo(m15mins)).isGreaterThan(0);
    }

    @Test
    public void cubicMeterIsSmallerThan_kiloCubicFeet() {
        ReadingType rt_15minCubicMeter = mock(ReadingType.class);
        when(rt_15minCubicMeter.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(rt_15minCubicMeter.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(rt_15minCubicMeter.getUnit()).thenReturn(ReadingTypeUnit.CUBICMETER);
        when(rt_15minCubicMeter.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        VirtualReadingType m3_15mins = VirtualReadingType.from(rt_15minCubicMeter);

        ReadingType rt_15minKCubicFeet = mock(ReadingType.class);
        when(rt_15minKCubicFeet.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(rt_15minKCubicFeet.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(rt_15minKCubicFeet.getUnit()).thenReturn(ReadingTypeUnit.CUBICFEET);
        when(rt_15minKCubicFeet.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        VirtualReadingType kf3_15mins = VirtualReadingType.from(rt_15minKCubicFeet);

        // Business method + asserts
        assertThat(m3_15mins.compareTo(kf3_15mins)).isLessThan(0);
    }

    @Test
    public void kiloCubicFeetIsBiggerThan_CubicMeter() {
        ReadingType rt_15minCubicMeter = mock(ReadingType.class);
        when(rt_15minCubicMeter.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(rt_15minCubicMeter.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(rt_15minCubicMeter.getUnit()).thenReturn(ReadingTypeUnit.CUBICMETER);
        when(rt_15minCubicMeter.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        VirtualReadingType m3_15mins = VirtualReadingType.from(rt_15minCubicMeter);

        ReadingType rt_15minKCubicFeet = mock(ReadingType.class);
        when(rt_15minKCubicFeet.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(rt_15minKCubicFeet.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(rt_15minKCubicFeet.getUnit()).thenReturn(ReadingTypeUnit.CUBICFEET);
        when(rt_15minKCubicFeet.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        VirtualReadingType kf3_15mins = VirtualReadingType.from(rt_15minKCubicFeet);

        // Business method + asserts
        assertThat(kf3_15mins.compareTo(m3_15mins)).isGreaterThan(0);
    }

    @Test
    public void literIsSmallerThan_kiloUSGallon() {
        ReadingType rt_15minLiter = mock(ReadingType.class);
        when(rt_15minLiter.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(rt_15minLiter.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(rt_15minLiter.getUnit()).thenReturn(ReadingTypeUnit.LITRE);
        when(rt_15minLiter.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        VirtualReadingType liter15mins = VirtualReadingType.from(rt_15minLiter);

        ReadingType rt_15minKGallon = mock(ReadingType.class);
        when(rt_15minKGallon.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(rt_15minKGallon.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(rt_15minKGallon.getUnit()).thenReturn(ReadingTypeUnit.USGALLON);
        when(rt_15minKGallon.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        VirtualReadingType KUSG_15mins = VirtualReadingType.from(rt_15minKGallon);

        // Business method + asserts
        assertThat(liter15mins.compareTo(KUSG_15mins)).isLessThan(0);
    }

    @Test
    public void kiloUSGallonIsBiggerThan_Liter() {
        ReadingType rt_15minLiter = mock(ReadingType.class);
        when(rt_15minLiter.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(rt_15minLiter.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(rt_15minLiter.getUnit()).thenReturn(ReadingTypeUnit.LITRE);
        when(rt_15minLiter.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        VirtualReadingType liter15mins = VirtualReadingType.from(rt_15minLiter);

        ReadingType rt_15minKGallon = mock(ReadingType.class);
        when(rt_15minKGallon.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(rt_15minKGallon.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(rt_15minKGallon.getUnit()).thenReturn(ReadingTypeUnit.USGALLON);
        when(rt_15minKGallon.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        VirtualReadingType KUSG_15mins = VirtualReadingType.from(rt_15minKGallon);

        // Business method + asserts
        assertThat(KUSG_15mins.compareTo(liter15mins)).isGreaterThan(0);
    }

    @Test
    public void barIsSmallerThan_kiloPascal() {
        ReadingType rt_15minBar = mock(ReadingType.class);
        when(rt_15minBar.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(rt_15minBar.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(rt_15minBar.getUnit()).thenReturn(ReadingTypeUnit.BAR);
        when(rt_15minBar.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        VirtualReadingType bar15mins = VirtualReadingType.from(rt_15minBar);

        ReadingType rt_15minKPascal = mock(ReadingType.class);
        when(rt_15minKPascal.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(rt_15minKPascal.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(rt_15minKPascal.getUnit()).thenReturn(ReadingTypeUnit.PASCAL);
        when(rt_15minKPascal.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        VirtualReadingType KPascal15mins = VirtualReadingType.from(rt_15minKPascal);

        // Business method + asserts
        assertThat(bar15mins.compareTo(KPascal15mins)).isLessThan(0);
    }

    @Test
    public void kiloPascalIsBiggerThan_bar() {
        ReadingType rt_15minBar = mock(ReadingType.class);
        when(rt_15minBar.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(rt_15minBar.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(rt_15minBar.getUnit()).thenReturn(ReadingTypeUnit.BAR);
        when(rt_15minBar.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        VirtualReadingType bar15mins = VirtualReadingType.from(rt_15minBar);

        ReadingType rt_15minKPascal = mock(ReadingType.class);
        when(rt_15minKPascal.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(rt_15minKPascal.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(rt_15minKPascal.getUnit()).thenReturn(ReadingTypeUnit.PASCAL);
        when(rt_15minKPascal.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        VirtualReadingType KPascal15mins = VirtualReadingType.from(rt_15minKPascal);

        // Business method + asserts
        assertThat(KPascal15mins.compareTo(bar15mins)).isGreaterThan(0);
    }

    @Test
    public void kelvinIsSmallerThan_Celcius() {
        ReadingType rt_15minKelvin = mock(ReadingType.class);
        when(rt_15minKelvin.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(rt_15minKelvin.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(rt_15minKelvin.getUnit()).thenReturn(ReadingTypeUnit.KELVIN);
        when(rt_15minKelvin.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        VirtualReadingType kelvin15mins = VirtualReadingType.from(rt_15minKelvin);

        ReadingType rt_15minCelcius = mock(ReadingType.class);
        when(rt_15minCelcius.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(rt_15minCelcius.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(rt_15minCelcius.getUnit()).thenReturn(ReadingTypeUnit.DEGREESCELSIUS);
        when(rt_15minCelcius.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        VirtualReadingType celcius15mins = VirtualReadingType.from(rt_15minCelcius);

        // Business method + asserts
        assertThat(kelvin15mins.compareTo(celcius15mins)).isLessThan(0);
    }

    @Test
    public void celciusIsBiggerThan_Kelvin() {
        ReadingType rt_15minKelvin = mock(ReadingType.class);
        when(rt_15minKelvin.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(rt_15minKelvin.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(rt_15minKelvin.getUnit()).thenReturn(ReadingTypeUnit.KELVIN);
        when(rt_15minKelvin.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        VirtualReadingType kelvin15mins = VirtualReadingType.from(rt_15minKelvin);

        ReadingType rt_15minCelcius = mock(ReadingType.class);
        when(rt_15minCelcius.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(rt_15minCelcius.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(rt_15minCelcius.getUnit()).thenReturn(ReadingTypeUnit.DEGREESCELSIUS);
        when(rt_15minCelcius.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        VirtualReadingType celcius15mins = VirtualReadingType.from(rt_15minCelcius);

        // Business method + asserts
        assertThat(celcius15mins.compareTo(kelvin15mins)).isGreaterThan(0);
    }

    @Test
    public void kelvinIsSmallerThan_Fahrenheit() {
        ReadingType rt_15minKelvin = mock(ReadingType.class);
        when(rt_15minKelvin.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(rt_15minKelvin.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(rt_15minKelvin.getUnit()).thenReturn(ReadingTypeUnit.KELVIN);
        when(rt_15minKelvin.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        VirtualReadingType kelvin15mins = VirtualReadingType.from(rt_15minKelvin);

        ReadingType rt_15minFahrenheit = mock(ReadingType.class);
        when(rt_15minFahrenheit.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(rt_15minFahrenheit.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(rt_15minFahrenheit.getUnit()).thenReturn(ReadingTypeUnit.DEGREESFAHRENHEIT);
        when(rt_15minFahrenheit.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        VirtualReadingType fahrenheit15mins = VirtualReadingType.from(rt_15minFahrenheit);

        // Business method + asserts
        assertThat(kelvin15mins.compareTo(fahrenheit15mins)).isLessThan(0);
    }

    @Test
    public void fahrenheitIsBiggerThan_Kelvin() {
        ReadingType rt_15minKelvin = mock(ReadingType.class);
        when(rt_15minKelvin.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(rt_15minKelvin.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(rt_15minKelvin.getUnit()).thenReturn(ReadingTypeUnit.KELVIN);
        when(rt_15minKelvin.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        VirtualReadingType kelvin15mins = VirtualReadingType.from(rt_15minKelvin);

        ReadingType rt_15minFahrenheit = mock(ReadingType.class);
        when(rt_15minFahrenheit.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(rt_15minFahrenheit.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(rt_15minFahrenheit.getUnit()).thenReturn(ReadingTypeUnit.DEGREESFAHRENHEIT);
        when(rt_15minFahrenheit.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        VirtualReadingType fahrenheit15mins = VirtualReadingType.from(rt_15minFahrenheit);

        // Business method + asserts
        assertThat(fahrenheit15mins.compareTo(kelvin15mins)).isGreaterThan(0);
    }

}