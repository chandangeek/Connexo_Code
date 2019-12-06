/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Aggregate;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.Phase;
import com.elster.jupiter.cbo.RationalNumber;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.metering.ReadingType;

import com.google.common.base.Joiner;

import java.util.Currency;

/**
 * CIMPattern represents pattern for searching registers/channels.
 * Format is "-.*.-.*.*.*.*.*.*.*.*.*.*.*.*.*.*.*":
 * MacroPeriod and TimeAttribute are not used in this pattern.
 *
 */

public class CIMPattern {
    private static final String ANY_CODE = "*";
    private static final String MISSED_CODE = "-";

    //private MacroPeriod macroPeriod;
    private Aggregate aggregate;
    //private TimeAttribute measuringPeriod;
    private Accumulation accumulation;
    private FlowDirection flowDirection;
    private Commodity commodity;
    private MeasurementKind measurementKind;
    private RationalNumber interharmonic;
    private RationalNumber argument;
    private Integer tou;
    private Integer cpp;
    private Integer consumptionTier;
    private Phase phases;
    private MetricMultiplier multiplier;
    private ReadingTypeUnit unit;
    private Currency currency;


    public CIMPattern(String[] codes) {
        if (isInteger(codes[1])) {
            this.aggregate = Aggregate.get((Integer.parseInt(codes[1])));
        }
        if (isInteger(codes[3])) {
            this.accumulation = Accumulation.get(Integer.parseInt(codes[3]));
        }
        if (isInteger(codes[4])) {
            this.flowDirection = FlowDirection.get(Integer.parseInt(codes[4]));
        }
        if (isInteger(codes[5])) {
            this.commodity = Commodity.get(Integer.parseInt(codes[5]));
        }

        if (isInteger(codes[6])) {
            this.measurementKind = MeasurementKind.get(Integer.parseInt(codes[6]));
        }

        if (isInteger(codes[7]) && isInteger(codes[8])) {
            this.interharmonic = new RationalNumber(Integer.parseInt(codes[7]), Integer.parseInt(codes[8]));
        }

        if (isInteger(codes[9]) && isInteger(codes[10])) {
            this.argument = new RationalNumber(Integer.parseInt(codes[9]), Integer.parseInt(codes[10]));
        }

        if (isInteger(codes[11])) {
            this.tou = Integer.parseInt(codes[11]);
        }

        if (isInteger(codes[12])) {
            this.cpp = Integer.parseInt(codes[12]);
        }

        if (isInteger(codes[13])) {
            this.consumptionTier = Integer.parseInt(codes[13]);
        }

        if (isInteger(codes[14])) {
            this.phases = Phase.get(Integer.parseInt(codes[14]));
        }

        if (isInteger(codes[15])) {
            this.multiplier = MetricMultiplier.with(Integer.parseInt(codes[15]));
        }
        if (isInteger(codes[16])) {
            this.unit = ReadingTypeUnit.get(Integer.parseInt(codes[16]));
        }
        if (isInteger(codes[17])) {
            this.currency = getCurrency(Integer.parseInt(codes[17]));
        }
    }

    public boolean isMatch(ReadingType readingType) {
        if (aggregate != null) {
            if (!aggregate.equals(readingType.getAggregate())) {
                return false;
            }
        }
        if (accumulation != null) {
            if (!accumulation.equals(readingType.getAccumulation())) {
                return false;
            }
        }
        if (flowDirection != null) {
            if (!flowDirection.equals(readingType.getFlowDirection())) {
                return false;
            }
        }
        if (commodity != null) {
            if (!commodity.equals(readingType.getCommodity())) {
                return false;
            }
        }
        if (measurementKind != null) {
            if (!measurementKind.equals(readingType.getMeasurementKind())) {
                return false;
            }
        }
        if (interharmonic != null) {
            if (!interharmonic.equals(readingType.getInterharmonic())) {
                return false;
            }
        }
        if (argument != null) {
            if (!argument.equals(readingType.getArgument())) {
                return false;
            }
        }
        if (tou != null) {
            if (!tou.equals(readingType.getTou())) {
                return false;
            }
        }
        if (cpp != null) {
            if (!cpp.equals(readingType.getCpp())) {
                return false;
            }
        }
        if (consumptionTier != null) {
            if (!consumptionTier.equals(readingType.getConsumptionTier())) {
                return false;
            }
        }
        if (phases != null) {
            if (!phases.equals(readingType.getPhases())) {
                return false;
            }
        }
        if (multiplier != null) {
            if (!multiplier.equals(readingType.getMultiplier())) {
                return false;
            }
        }
        if (unit != null) {
            if (!unit.equals(readingType.getUnit())) {
                return false;
            }
        }
        if (currency != null) {
            if (!currency.equals(readingType.getCurrency())) {
                return false;
            }
        }

        return true;
    }

    public String code() {
        return Joiner.on(".").join(
                MISSED_CODE,
                aggregate == null ? ANY_CODE : aggregate.getId(),
                MISSED_CODE,
                accumulation == null ? ANY_CODE : accumulation.getId(),
                flowDirection == null ? ANY_CODE : flowDirection.getId(),
                commodity == null ? ANY_CODE : commodity.getId(),
                measurementKind == null ? ANY_CODE : measurementKind.getId(),
                interharmonic == null ? ANY_CODE : interharmonic.getNumerator(),
                interharmonic == null ? ANY_CODE : interharmonic.getDenominator(),
                argument == null ? ANY_CODE : argument.getNumerator(),
                argument == null ? ANY_CODE : argument.getDenominator(),
                tou == null ? ANY_CODE : tou,
                cpp == null ? ANY_CODE : cpp,
                consumptionTier == null ? ANY_CODE : consumptionTier,
                phases == null ? ANY_CODE : phases.getId(),
                multiplier == null ? ANY_CODE : multiplier.getMultiplier(),
                unit == null ? ANY_CODE : unit.getId(),
                currency == null ? ANY_CODE : getCurrencyId(currency));
    }

    private static boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static Currency getCurrency(int isoCode) {
        return Currency.getAvailableCurrencies()
                .stream()
                .filter(c -> c.getNumericCode() == isoCode)
                .findAny()
                .orElse(null);
    }

    private static int getCurrencyId(Currency currency) {
        int result = currency.getNumericCode();
        return result == 999 ? 0 : result;
    }
}