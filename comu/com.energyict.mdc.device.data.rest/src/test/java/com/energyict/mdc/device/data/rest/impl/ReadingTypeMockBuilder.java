/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Aggregate;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.Phase;
import com.elster.jupiter.cbo.RationalNumber;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.ReadingType;

import java.util.Currency;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Provides helper logic to mock ReadingTypes
 */
public final class ReadingTypeMockBuilder {

    private static final int NUMBER_OF_READING_TYPE_ARGUMENTS = 18;

    private static final int CURRENCY_INDEX = 17;
    private static final int READING_TYPE_UNIT_INDEX = 16;
    private static final int SCALER_INDEX = 15;
    private static final int PHASE_INDEX = 14;
    private static final int CONSUMPTION_TIER_INDEX = 13;
    private static final int CPP_INDEX = 12;
    private static final int TOU_INDEX = 11;
    private static final int ARGUMENT_DENOMINATOR_INDEX = 10;
    private static final int ARGUMENT_NUMERATOR_INDEX = 9;
    private static final int HARMONIC_DENOMINATOR_INDEX = 8;
    private static final int HARMONIC_NUMERATOR_INDEX = 7;
    private static final int KIND_INDEX = 6;
    private static final int COMMODITY_INDEX = 5;
    private static final int FLOW_DIRECTION_INDEX = 4;
    private static final int ACCUMULATION_INDEX = 3;
    private static final int TIME_ATTRIBUTE_INDEX = 2;
    private static final int AGGREGATE_INDEX = 1;
    private static final int MACRO_PERIOD_INDEX = 0;

    private final ReadingType mock;

    private ReadingTypeMockBuilder() {
        mock = mock(ReadingType.class);
    }

    public static ReadingTypeMockBuilder from(String mrid){
        ReadingTypeMockBuilder readingTypeMockBuilder = new ReadingTypeMockBuilder();
        String[] arguments = mrid.split("\\.");
        if (arguments.length != NUMBER_OF_READING_TYPE_ARGUMENTS) {
            throw new IllegalArgumentException("The provided ReadingType code should contain " + NUMBER_OF_READING_TYPE_ARGUMENTS + " fields.");
        }
        when(readingTypeMockBuilder.mock.getMacroPeriod()).thenReturn(MacroPeriod.get(Integer.valueOf(arguments[MACRO_PERIOD_INDEX])));
        when(readingTypeMockBuilder.mock.getAggregate()).thenReturn(Aggregate.get(Integer.valueOf(arguments[AGGREGATE_INDEX])));
        when(readingTypeMockBuilder.mock.getMeasuringPeriod()).thenReturn(TimeAttribute.get(Integer.valueOf(arguments[TIME_ATTRIBUTE_INDEX])));
        when(readingTypeMockBuilder.mock.getAccumulation()).thenReturn(Accumulation.get(Integer.valueOf(arguments[ACCUMULATION_INDEX])));
        when(readingTypeMockBuilder.mock.getFlowDirection()).thenReturn(FlowDirection.get(Integer.valueOf(arguments[FLOW_DIRECTION_INDEX])));
        when(readingTypeMockBuilder.mock.getCommodity()).thenReturn(Commodity.get(Integer.valueOf(arguments[COMMODITY_INDEX])));
        when(readingTypeMockBuilder.mock.getMeasurementKind()).thenReturn(MeasurementKind.get(Integer.valueOf(arguments[KIND_INDEX])));
        if(Long.valueOf(arguments[HARMONIC_NUMERATOR_INDEX]) > 0){
            when(readingTypeMockBuilder.mock.getInterharmonic()).thenReturn(new RationalNumber(Long.valueOf(arguments[HARMONIC_NUMERATOR_INDEX]), Long.valueOf(arguments[HARMONIC_DENOMINATOR_INDEX])));
        } else {
            when(readingTypeMockBuilder.mock.getInterharmonic()).thenReturn(RationalNumber.NOTAPPLICABLE);
        }
        if(Long.valueOf(arguments[ARGUMENT_NUMERATOR_INDEX]) > 0){
            when(readingTypeMockBuilder.mock.getArgument()).thenReturn(new RationalNumber(Long.valueOf(arguments[ARGUMENT_NUMERATOR_INDEX]), Long.valueOf(arguments[ARGUMENT_DENOMINATOR_INDEX])));
        } else {
            when(readingTypeMockBuilder.mock.getArgument()).thenReturn(RationalNumber.NOTAPPLICABLE);
        }
        when(readingTypeMockBuilder.mock.getTou()).thenReturn(Integer.valueOf(arguments[TOU_INDEX]));
        when(readingTypeMockBuilder.mock.getCpp()).thenReturn(Integer.valueOf(arguments[CPP_INDEX]));
        when(readingTypeMockBuilder.mock.getConsumptionTier()).thenReturn(Integer.valueOf(arguments[CONSUMPTION_TIER_INDEX]));
        when(readingTypeMockBuilder.mock.getPhases()).thenReturn(Phase.get(Integer.valueOf(arguments[PHASE_INDEX])));
        when(readingTypeMockBuilder.mock.getMultiplier()).thenReturn(MetricMultiplier.with(Integer.valueOf(arguments[SCALER_INDEX])));
        when(readingTypeMockBuilder.mock.getUnit()).thenReturn(ReadingTypeUnit.get(Integer.valueOf(arguments[READING_TYPE_UNIT_INDEX])));
        Currency.getAvailableCurrencies().stream().filter(each -> each.getNumericCode() == Integer.valueOf(arguments[CURRENCY_INDEX])).forEach(each -> when(readingTypeMockBuilder.mock.getCurrency()).thenReturn(each));
        when(readingTypeMockBuilder.mock.getMRID()).thenReturn(mrid);
        when(readingTypeMockBuilder.mock.getFullAliasName()).thenReturn(mrid);
        return readingTypeMockBuilder;
    }



    public ReadingType getMock(){
        return mock;
    }
}
