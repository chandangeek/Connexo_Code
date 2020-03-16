/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.custom.meterreadingdocument;

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
import java.util.Optional;

/**
 * CIMCodePattern represents pattern for searching registers/channels.
 * Format is "*.*.*.*.*.*.*.*.*.*.*.*.*.*.*.*.*.*":
 */
class CIMCodePattern {
    private MacroPeriod macroPeriod;
    private Aggregate aggregate;
    private TimeAttribute timeAttribute;
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


    private CIMCodePattern(String[] codes) {
        parseOptionalInteger(codes[0]).ifPresent(macroPeriod -> this.macroPeriod = MacroPeriod.get(macroPeriod));
        parseOptionalInteger(codes[1]).ifPresent(aggregate -> this.aggregate = Aggregate.get(aggregate));
        parseOptionalInteger(codes[2]).ifPresent(timeAttribute -> this.timeAttribute = TimeAttribute.get(timeAttribute));
        parseOptionalInteger(codes[3]).ifPresent(accumulation -> this.accumulation = Accumulation.get(accumulation));
        parseOptionalInteger(codes[4]).ifPresent(flowDirection -> this.flowDirection = FlowDirection.get(flowDirection));
        parseOptionalInteger(codes[5]).ifPresent(commodity -> this.commodity = Commodity.get(commodity));
        parseOptionalInteger(codes[6]).ifPresent(measurementKind -> this.measurementKind = MeasurementKind.get(measurementKind));

        Optional<Integer> interharmonicNum = parseOptionalInteger(codes[7]);
        Optional<Integer> interharmonicDen = parseOptionalInteger(codes[8]);
        if (interharmonicNum.isPresent() && interharmonicDen.isPresent()) {
            if (interharmonicNum.get() == 0 && interharmonicDen.get() == 0) {
                this.interharmonic = RationalNumber.NOTAPPLICABLE;
            } else {
                this.interharmonic = new RationalNumber(interharmonicNum.get(), interharmonicDen.get());
            }
        }

        Optional<Integer> argumentNum = parseOptionalInteger(codes[9]);
        Optional<Integer> argumentDen = parseOptionalInteger(codes[10]);
        if (argumentNum.isPresent() && argumentDen.isPresent()) {
            if (argumentNum.get() == 0 && argumentDen.get() == 0) {
                this.argument = RationalNumber.NOTAPPLICABLE;
            } else {
                this.argument = new RationalNumber(argumentNum.get(), argumentDen.get());
            }
        }

        parseOptionalInteger(codes[11]).ifPresent(tou -> this.tou = tou);
        parseOptionalInteger(codes[12]).ifPresent(cpp -> this.cpp = cpp);
        parseOptionalInteger(codes[13]).ifPresent(consumptionTier -> this.consumptionTier = consumptionTier);
        parseOptionalInteger(codes[14]).ifPresent(phases -> this.phases = Phase.get(phases));
        parseOptionalInteger(codes[15]).ifPresent(multiplier -> this.multiplier = MetricMultiplier.with(multiplier));
        parseOptionalInteger(codes[16]).ifPresent(unit -> this.unit = ReadingTypeUnit.get(unit));
        parseOptionalInteger(codes[17]).ifPresent(currency -> this.currency = getCurrency(currency));
    }

    static CIMCodePattern parseFromString(String[] codes) {
        return new CIMCodePattern(codes);
    }

    boolean matches(ReadingType readingType) {
        if (macroPeriod != null) {
            if (!macroPeriod.equals(readingType.getMacroPeriod())) {
                return false;
            }
        }
        if (aggregate != null) {
            if (!aggregate.equals(readingType.getAggregate())) {
                return false;
            }
        }
        if (timeAttribute != null) {
            if (!timeAttribute.equals(readingType.getMeasuringPeriod())) {
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
}
