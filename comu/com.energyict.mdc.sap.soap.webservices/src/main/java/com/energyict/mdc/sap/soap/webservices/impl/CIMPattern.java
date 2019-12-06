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
import java.util.Optional;

/**
 * CIMPattern represents pattern for searching registers/channels.
 * Format is "-.*.-.*.*.*.*.*.*.*.*.*.*.*.*.*.*.*":
 * MacroPeriod and TimeAttribute are not used in this pattern.
 *
 */
public class CIMPattern {
    private static final String ANY_CODE = "*";
    private static final String MISSED_CODE = "-";

    private Aggregate aggregate;
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


    CIMPattern(String[] codes) {
        parseOptionalInteger(codes[1]).ifPresent(aggregate->this.aggregate = Aggregate.get(aggregate));
        parseOptionalInteger(codes[3]).ifPresent(accumulation->this.accumulation = Accumulation.get(accumulation));
        parseOptionalInteger(codes[4]).ifPresent(flowDirection->this.flowDirection = FlowDirection.get(flowDirection));
        parseOptionalInteger(codes[5]).ifPresent(commodity->this.commodity = Commodity.get(commodity));
        parseOptionalInteger(codes[6]).ifPresent(measurementKind->this.measurementKind = MeasurementKind.get(measurementKind));

        Optional<Integer> interharmonicNum = parseOptionalInteger(codes[7]);
        Optional<Integer> interharmonicDen = parseOptionalInteger(codes[8]);
        if (interharmonicNum.isPresent() && interharmonicDen.isPresent()) {
            this.interharmonic = new RationalNumber(interharmonicNum.get(), interharmonicDen.get());
        }

        Optional<Integer> argumentNum = parseOptionalInteger(codes[9]);
        Optional<Integer> argumentDen = parseOptionalInteger(codes[10]);
        if (argumentNum.isPresent() && argumentDen.isPresent()) {
            this.argument = new RationalNumber(argumentNum.get(), argumentDen.get());
        }

        parseOptionalInteger(codes[11]).ifPresent(tou->this.tou = tou);
        parseOptionalInteger(codes[12]).ifPresent(cpp->this.cpp = cpp);
        parseOptionalInteger(codes[13]).ifPresent(consumptionTier->this.consumptionTier = consumptionTier);
        parseOptionalInteger(codes[14]).ifPresent(phases->this.phases = Phase.get(phases));
        parseOptionalInteger(codes[15]).ifPresent(multiplier->this.multiplier = MetricMultiplier.with(multiplier));
        parseOptionalInteger(codes[16]).ifPresent(unit->this.unit = ReadingTypeUnit.get(unit));
        parseOptionalInteger(codes[17]).ifPresent(currency->this.currency = getCurrency(currency));
    }

    public static CIMPattern parseFromString(String[] codes) {
        return new CIMPattern(codes);
    }

    public boolean matches(ReadingType readingType) {
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

    private static Optional<Integer> parseOptionalInteger(String str) {
        try {
            return Optional.of(Integer.parseInt(str));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private static Currency getCurrency(int isoCode) {
        if (isoCode == 0) {
            isoCode = 999;
        }
        for (Currency each : Currency.getAvailableCurrencies()) {
            if (each.getNumericCode() == isoCode) {
                return each;
            }
        }
        return null;
    }

    private static int getCurrencyId(Currency currency) {
        int result = currency.getNumericCode();
        return result == 999 ? 0 : result;
    }
}