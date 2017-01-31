/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.metering.impl;

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
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.Pair;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;

import java.util.Currency;
import java.util.Optional;

public final class ObisCodeToReadingTypeFactory {

    public static final int FIXED_CONSUMPTION_TIER_VALUE = 0;
    public static final int FIXED_CRITICIL_PEAK_PERIOD_VALUE = 0;
    public static final RationalNumber FIXED_ARGUMENT_VALUE = RationalNumber.NOTAPPLICABLE;

    public static String createMRIDFromObisCodeAndUnit(final ObisCode obisCode, final Unit unit) {
        return createMRIDFromObisCodeUnitAndInterval(obisCode, unit, null);
    }

    private static void appendNullableRationalNumber(RationalNumber rationalNumber, StringBuilder stringBuilder) {
        if (rationalNumber != null) {
            stringBuilder.append(rationalNumber.getNumerator()).append(".");
            stringBuilder.append(rationalNumber.getDenominator()).append(".");
        } else {
            stringBuilder.append("0.0.");
        }
    }

    public static String createMRIDFromObisCodeUnitAndInterval(ObisCode obisCode, Unit unit, TimeDuration interval) {
        MacroPeriod macroPeriod = MacroPeriodMapping.getMacroPeriodFor(obisCode, interval);
        Aggregate aggregate = AggregateMapping.getAggregateFor(obisCode, macroPeriod);
        TimeAttribute measuringPeriod = MeasuringPeriodMapping.getMeasuringPeriodFor(obisCode, interval);
        Accumulation accumulation = AccumulationMapping.getAccumulationFor(obisCode, interval);
        FlowDirection flowDirection = FlowDirectionMapping.getFlowDirectionFor(obisCode);
        Commodity commodity = CommodityMapping.getCommodityFor(obisCode);
        MeasurementKind measurementKind = MeasurementKindMapping.getMeasurementKindFor(obisCode, unit);
        RationalNumber interHarmonic = InterHarmonicMapping.getInterHarmonicFor(obisCode);
        RationalNumber argument = FIXED_ARGUMENT_VALUE;
        int timeOfUse = TimeOfUseMapping.getTimeOfUseFor(obisCode);
        int criticalPeakPeriod = FIXED_CRITICIL_PEAK_PERIOD_VALUE;
        int consumptionTier = FIXED_CONSUMPTION_TIER_VALUE;
        Phase phase = PhaseMapping.getPhaseFor(obisCode);
        /* ReadingTypeUnit and MetricMultiplier are combined */
        Pair<ReadingTypeUnit, MetricMultiplier> scaledCIMUnit = ReadingTypeUnitMapping.getScaledCIMUnitFor(unit);
        ReadingTypeUnit readingTypeUnit = scaledCIMUnit.getFirst();
        MetricMultiplier metricMultiplier = scaledCIMUnit.getLast();

        Optional<Currency> currency = CurrencyMapping.getCurrencyFor(unit);

        StringBuilder mrid = new StringBuilder();
        mrid.append(macroPeriod.getId()).append(".");
        mrid.append(aggregate.getId()).append(".");
        mrid.append(measuringPeriod.getId()).append(".");
        mrid.append(accumulation.getId()).append(".");
        mrid.append(flowDirection.getId()).append(".");
        mrid.append(commodity.getId()).append(".");
        mrid.append(measurementKind.getId()).append(".");
        appendNullableRationalNumber(interHarmonic, mrid);
        appendNullableRationalNumber(argument, mrid);
        mrid.append(timeOfUse).append(".");
        mrid.append(criticalPeakPeriod).append(".");
        mrid.append(consumptionTier).append(".");
        mrid.append(phase.getId()).append(".");
        mrid.append(metricMultiplier.getMultiplier()).append(".");
        mrid.append(readingTypeUnit.getId()).append(".");
        mrid.append(currency.isPresent() ? currency.get().getNumericCode() : 0);
        return mrid.toString();
    }
}
