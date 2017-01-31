/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.metering.impl;

import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.util.Pair;
import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.metering.impl.matchers.CompositeMatcher;
import com.energyict.mdc.metering.impl.matchers.ItemMatcher;
import com.energyict.mdc.metering.impl.matchers.Matcher;
import com.energyict.mdc.metering.impl.matchers.Range;
import com.energyict.mdc.metering.impl.matchers.RangeMatcher;

enum ReadingTypeUnitMapping {

    HOUR(BaseUnit.HOUR, ReadingTypeUnit.HOUR, Matcher.DONT_CARE),
    MINUTE(BaseUnit.MINUTE, ReadingTypeUnit.MINUTE, Matcher.DONT_CARE),
    SECOND(BaseUnit.SECOND, ReadingTypeUnit.SECOND, Matcher.DONT_CARE),
    DEGREE(BaseUnit.DEGREE, ReadingTypeUnit.DEGREES, Matcher.DONT_CARE),
    DEGREECELSIUS(BaseUnit.DEGREE_CELSIUS, ReadingTypeUnit.DEGREESCELSIUS, Matcher.DONT_CARE),
    METER(BaseUnit.METER, ReadingTypeUnit.METER, Matcher.DONT_CARE),
    METERPERSECOND(BaseUnit.METERPERSECOND, ReadingTypeUnit.METERPERSECOND, Matcher.DONT_CARE),
    CUBICMETER(BaseUnit.CUBICMETER, ReadingTypeUnit.CUBICMETER, Matcher.DONT_CARE),
    NORMALCUBICMETER(BaseUnit.NORMALCUBICMETER, ReadingTypeUnit.CUBICMETERCOMPENSATED, Matcher.DONT_CARE),
    CUBICMETERPERHOUR(BaseUnit.CUBICMETERPERHOUR, ReadingTypeUnit.CUBICMETERPERHOUR, Matcher.DONT_CARE),
    NORMALCUBICMETERPERHOUR(BaseUnit.NORMALCUBICMETERPERHOUR, ReadingTypeUnit.CUBICMETERPERHOURCOMPENSATED, Matcher.DONT_CARE),
    LITER(BaseUnit.LITER, ReadingTypeUnit.LITRE, Matcher.DONT_CARE),
    KILOGRAM(BaseUnit.KILOGRAM, ReadingTypeUnit.GRAM, MetricMultiplier.KILO, Matcher.DONT_CARE),
    NEWTON(BaseUnit.NEWTON, ReadingTypeUnit.NEWTON, Matcher.DONT_CARE),
    PASCAL(BaseUnit.PASCAL, ReadingTypeUnit.PASCAL, Matcher.DONT_CARE),
    JOULE(BaseUnit.JOULE, ReadingTypeUnit.JOULE, Matcher.DONT_CARE),
    WATT(BaseUnit.WATT, ReadingTypeUnit.WATT,
            CompositeMatcher.createMatcherFor(
                    ItemMatcher.itemMatcherFor(1, 2, 21, 22, 41, 42, 61, 62),
                    RangeMatcher.rangeMatcherFor(new Range(15, 20)),
                    RangeMatcher.rangeMatcherFor(new Range(35, 40)),
                    RangeMatcher.rangeMatcherFor(new Range(55, 60)),
                    RangeMatcher.rangeMatcherFor(new Range(75, 80)))),
    VOLTAMPERE(BaseUnit.VOLTAMPERE, ReadingTypeUnit.VOLTAMPERE,
            CompositeMatcher.createMatcherFor(
                    ItemMatcher.itemMatcherFor(9, 10),
                    ItemMatcher.itemMatcherFor(29, 30),
                    ItemMatcher.itemMatcherFor(49, 50),
                    ItemMatcher.itemMatcherFor(69, 70))),
    VOLTAMPEREREACTIVE(BaseUnit.VOLTAMPEREREACTIVE, ReadingTypeUnit.VOLTAMPEREREACTIVE,
            CompositeMatcher.createMatcherFor(
                    RangeMatcher.rangeMatcherFor(new Range(3, 8)),
                    RangeMatcher.rangeMatcherFor(new Range(23, 28)),
                    RangeMatcher.rangeMatcherFor(new Range(43, 48)),
                    RangeMatcher.rangeMatcherFor(new Range(63, 68)))),
    WATTHOUR(BaseUnit.WATTHOUR, ReadingTypeUnit.WATTHOUR,
            CompositeMatcher.createMatcherFor(
                    ItemMatcher.itemMatcherFor(1, 2, 21, 22, 41, 42, 61, 62),
                    RangeMatcher.rangeMatcherFor(new Range(15, 20)),
                    RangeMatcher.rangeMatcherFor(new Range(35, 40)),
                    RangeMatcher.rangeMatcherFor(new Range(55, 60)),
                    RangeMatcher.rangeMatcherFor(new Range(75, 80)))),
    VOLTAMPEREHOUR(BaseUnit.VOLTAMPEREHOUR, ReadingTypeUnit.VOLTAMPEREHOUR,
            CompositeMatcher.createMatcherFor(
                    ItemMatcher.itemMatcherFor(9, 10),
                    ItemMatcher.itemMatcherFor(29, 30),
                    ItemMatcher.itemMatcherFor(49, 50),
                    ItemMatcher.itemMatcherFor(69, 70))),
    VOLTAMPEREREACTIVEHOUR(BaseUnit.VOLTAMPEREREACTIVEHOUR, ReadingTypeUnit.VOLTAMPEREREACTIVEHOUR,
            CompositeMatcher.createMatcherFor(
                    ItemMatcher.itemMatcherFor(15, 16, 35, 36, 55, 53, 75, 76),
                    RangeMatcher.rangeMatcherFor(new Range(3, 8)),
                    RangeMatcher.rangeMatcherFor(new Range(23, 28)),
                    RangeMatcher.rangeMatcherFor(new Range(43, 48)),
                    RangeMatcher.rangeMatcherFor(new Range(63, 68)))),
    AMPERE(BaseUnit.AMPERE, ReadingTypeUnit.AMPERE, ItemMatcher.itemMatcherFor(11, 31, 51, 71, 90, 91)),
    COULOMB(BaseUnit.COULOMB, ReadingTypeUnit.COULOMB, Matcher.DONT_CARE),
    VOLT(BaseUnit.VOLT, ReadingTypeUnit.VOLT, ItemMatcher.itemMatcherFor(12, 32, 52, 72, 92)),
    FARAD(BaseUnit.FARAD, ReadingTypeUnit.FARAD, Matcher.DONT_CARE),
    OHM(BaseUnit.OHM, ReadingTypeUnit.OHM, Matcher.DONT_CARE),
    OHMSQUAREMETERPERMETER(BaseUnit.OHMSQUAREMETERPERMETER, ReadingTypeUnit.OHMMETER, Matcher.DONT_CARE),
    WEBER(BaseUnit.WEBER, ReadingTypeUnit.WEBER, Matcher.DONT_CARE),
    TESLA(BaseUnit.TESLA, ReadingTypeUnit.TESLA, Matcher.DONT_CARE),
    AMPEREPERMETER(BaseUnit.AMPEREPERMETER, ReadingTypeUnit.AMPEREPERMETER, Matcher.DONT_CARE),
    HENRY(BaseUnit.HENRY, ReadingTypeUnit.HENRY, Matcher.DONT_CARE),
    HERTZ(BaseUnit.HERTZ, ReadingTypeUnit.HERTZ, ItemMatcher.itemMatcherFor(14,34,54,74)),
    VOLTSQUAREDHOUR(BaseUnit.VOLTSQUAREHOUR, ReadingTypeUnit.VOLTSQUAREDHOUR, ItemMatcher.itemMatcherFor(89)),
    AMPERESQUAREDHOUR(BaseUnit.AMPERESQUAREHOUR, ReadingTypeUnit.AMPERESQUAREDHOUR, ItemMatcher.itemMatcherFor(88)),
    SIEMENS(BaseUnit.SIEMENS, ReadingTypeUnit.SIEMENS, Matcher.DONT_CARE),
    KELVIN(BaseUnit.KELVIN, ReadingTypeUnit.KELVIN, Matcher.DONT_CARE),
    DECIBELPOWERRATIO(BaseUnit.DECIBELPOWERRATIO, ReadingTypeUnit.BELMILLIWATT, MetricMultiplier.DECI, Matcher.DONT_CARE),
    NOTAVAILABLE(BaseUnit.NOTAVAILABLE, ReadingTypeUnit.NOTAPPLICABLE, Matcher.DONT_CARE),
    COUNT(BaseUnit.COUNT, ReadingTypeUnit.NOTAPPLICABLE, Matcher.DONT_CARE),
    RATIO(BaseUnit.RATIO, ReadingTypeUnit.NOTAPPLICABLE, Matcher.DONT_CARE),
    UNITLESS(BaseUnit.UNITLESS, ReadingTypeUnit.NOTAPPLICABLE, Matcher.DONT_CARE),
    MOLPERCENT(BaseUnit.MOLPERCENT, ReadingTypeUnit.MOLE, MetricMultiplier.CENTI, Matcher.DONT_CARE),  // The MetricMultiplier of centi is equal to percent (10^(-2))
    WATTHOURPERCUBICMETER(BaseUnit.WATTHOURPERCUBICMETER, ReadingTypeUnit.WATTHOURPERCUBICMETER, Matcher.DONT_CARE),
    CUBICFEET(BaseUnit.CUBICFEET, ReadingTypeUnit.CUBICFEET, Matcher.DONT_CARE),
    CUBICFEETPERHOUR(BaseUnit.CUBICFEETPERHOUR, ReadingTypeUnit.CUBICFEETPERHOUR, Matcher.DONT_CARE),
    THERM(BaseUnit.THERM, ReadingTypeUnit.THERM, Matcher.DONT_CARE),
    VOLTSQUARE(BaseUnit.VOLTSQUARE, ReadingTypeUnit.VOLTSQUARED, Matcher.DONT_CARE),
    AMPERESQUARE(BaseUnit.AMPERESQUARE, ReadingTypeUnit.AMPERESQUARED, Matcher.DONT_CARE),
    POUNDPERSQUAREINCH(BaseUnit.POUNDPERSQUAREINCH, ReadingTypeUnit.POUNDPERSQUAREINCHABSOLUTE, Matcher.DONT_CARE), // taking the absolute value
    PERCENT(BaseUnit.PERCENT, ReadingTypeUnit.NOTAPPLICABLE, MetricMultiplier.CENTI, Matcher.DONT_CARE),   // The MetricMultiplier of centi is equal to percent (10^(-2))
    PARTSPERMILLION(BaseUnit.PARTSPERMILLION, ReadingTypeUnit.NOTAPPLICABLE, MetricMultiplier.MICRO, Matcher.DONT_CARE), //The MetricMultiplier of micro is equal to PertsPerMillion (10^(-6))
    QUANTITYPOWER(BaseUnit.QUANTITYPOWER, ReadingTypeUnit.QUANTITYPOWER, Matcher.DONT_CARE),
    QUANTITYPOWERHOUR(BaseUnit.QUANTITYPOWERHOUR, ReadingTypeUnit.QUANTITYENERGY, Matcher.DONT_CARE),
    USGALLON(BaseUnit.US_GALLON, ReadingTypeUnit.USGALLON, Matcher.DONT_CARE),
    USGALLONPERHOUR(BaseUnit.US_GALLONPERHOUR, ReadingTypeUnit.USGALLONPERHOUR, Matcher.DONT_CARE),
    IMPGALLON(BaseUnit.IMP_GALLON, ReadingTypeUnit.IMPERIALGALLON, Matcher.DONT_CARE),
    IMPGALLONPERHOUR(BaseUnit.IMP_GALLONPERHOUR, ReadingTypeUnit.IMPERIALGALLONPERHOUR, Matcher.DONT_CARE),
    POUND(BaseUnit.POUND, ReadingTypeUnit.NOTAPPLICABLE, Matcher.DONT_CARE),
    EURO(BaseUnit.EURO, ReadingTypeUnit.NOTAPPLICABLE, Matcher.DONT_CARE),
    USDOLLAR(BaseUnit.USD, ReadingTypeUnit.NOTAPPLICABLE, Matcher.DONT_CARE);

    private final Matcher<Integer> cField;

    /**
     * This represents a NON-mappable (mdc)Unit to CIM ReadingTypeUnit. We should not use it in a ReadingType.
     */
    public static final Pair<ReadingTypeUnit, MetricMultiplier> NO_CIM_UNIT_FOUND = Pair.of(null, null);

    private final int mdcUnit;
    private final ReadingTypeUnit cimUnit;
    private final MetricMultiplier metricMultiplier;

    ReadingTypeUnitMapping(int mdcUnit, ReadingTypeUnit cimUnit, Matcher<Integer> cField) {
        this.mdcUnit = mdcUnit;
        this.cimUnit = cimUnit;
        this.cField = cField;
        this.metricMultiplier = MetricMultiplier.ZERO;
    }

    ReadingTypeUnitMapping(int mdcUnit, ReadingTypeUnit cimUnit, MetricMultiplier metricMultiplier, Matcher<Integer> cField) {
        this.mdcUnit = mdcUnit;
        this.cimUnit = cimUnit;
        this.metricMultiplier = metricMultiplier;
        this.cField = cField;
    }

    public static Pair<ReadingTypeUnit, MetricMultiplier> getScaledCIMUnitFor(Unit unit) {
        for (ReadingTypeUnitMapping readingTypeUnitMapping : values()) {
            if (readingTypeUnitMapping.mdcUnit == unit.getBaseUnit().getDlmsCode()) {
                return Pair.of(readingTypeUnitMapping.cimUnit, getCombinedMultiplier(unit, readingTypeUnitMapping));
            }
        }
        return NO_CIM_UNIT_FOUND;
    }

    public static Unit getMdcUnitFor(ReadingTypeUnit readingTypeUnit, MetricMultiplier metricMultiplier){
        for (ReadingTypeUnitMapping readingTypeUnitMapping : values()) {
            if(readingTypeUnitMapping.cimUnit.equals(readingTypeUnit)){
                return Unit.get(readingTypeUnitMapping.mdcUnit, metricMultiplier.getMultiplier());
            }
        }
        return Unit.getUndefined();
    }

    private static MetricMultiplier getCombinedMultiplier(Unit unit, ReadingTypeUnitMapping readingTypeUnitMapping) {
        return MetricMultiplier.with(readingTypeUnitMapping.metricMultiplier.getMultiplier() + unit.getScale());
    }

    int getMdcUnit() {
        return mdcUnit;
    }

    ReadingTypeUnit getCimUnit() {
        return cimUnit;
    }

    MetricMultiplier getMetricMultiplier() {
        return metricMultiplier;
    }

    Matcher<Integer> getcField() {
        return cField;
    }
}
