/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.metering.impl;

import com.elster.jupiter.cbo.MeasurementKind;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.metering.impl.matchers.CompositeMatcher;
import com.energyict.mdc.metering.impl.matchers.ItemMatcher;
import com.energyict.mdc.metering.impl.matchers.Matcher;
import com.energyict.mdc.metering.impl.matchers.Range;
import com.energyict.mdc.metering.impl.matchers.RangeMatcher;
import com.energyict.mdc.metering.impl.matchers.UnitMatcher;

enum MeasurementKindMapping {

    CURRENT(MeasurementKind.CURRENT,
            ItemMatcher.ELECTRICITY_MATCH,
            ItemMatcher.itemMatcherFor(11, 31, 51, 71, 90, 91),
            ItemMatcher.itemMatcherFor(7),  // assuming only instantaneous values
            ItemMatcher.itemsDontMatchFor(124),
            Matcher.DONT_CARE),

    CURRENT_ANGLE(MeasurementKind.CURRENTANGLE,
            ItemMatcher.ELECTRICITY_MATCH,
            ItemMatcher.itemMatcherFor(81),
            ItemMatcher.itemMatcherFor(7),
            RangeMatcher.rangeMatcherFor(
                    between(4, 7),
                    between(14, 17),
                    between(24, 27),
                    between(34, 37),
                    between(44, 47),
                    between(54, 57),
                    between(64, 67),
                    between(74, 77)),
            Matcher.DONT_CARE),

    DEMAND(MeasurementKind.DEMAND,
            ItemMatcher.ELECTRICITY_MATCH,
            ItemMatcher.itemsDontMatchFor(0, 93, 94, 96, 97, 98, 99, 81, 83),
            ItemMatcher.itemMatcherFor(4, 5, 14, 15, 24, 25, 27, 28),
            ItemMatcher.itemsDontMatchFor(124),
            Matcher.DONT_CARE),

    ENERGY(MeasurementKind.ENERGY,
            ItemMatcher.ELECTRICITY_MATCH,
            RangeMatcher.rangeMatcherFor(
                    between(1, 10),
                    between(15, 30),
                    between(35, 50),
                    between(55, 70),
                    between(75, 80)),
            ItemMatcher.itemsDontMatchFor(4, 5, 14, 15, 24, 25, 27, 28),
            ItemMatcher.itemsDontMatchFor(124),
            UnitMatcher.volumeUnitMatcher()),

    FREQUENCY(MeasurementKind.FREQUENCY,
            ItemMatcher.ELECTRICITY_MATCH,
            ItemMatcher.itemMatcherFor(14, 34, 54, 74),
            Matcher.DONT_CARE,
            Matcher.DONT_CARE,
            Matcher.DONT_CARE),

    LINE_LOSSES(MeasurementKind.LINELOSSES,
            ItemMatcher.ELECTRICITY_MATCH,
            ItemMatcher.itemMatcherFor(83),
            Matcher.DONT_CARE,
            CompositeMatcher.createMatcherFor(
                    ItemMatcher.itemMatcherFor(20),
                    RangeMatcher.rangeMatcherFor(
                            between(1, 3),
                            between(10, 12),
                            between(31, 33),
                            between(40, 42),
                            between(51, 53),
                            between(60, 62),
                            between(71, 73),
                            between(80, 82)
                    )),
            Matcher.DONT_CARE),

    LOSSES(MeasurementKind.LOSSES,
            ItemMatcher.ELECTRICITY_MATCH,
            ItemMatcher.itemMatcherFor(83),
            Matcher.DONT_CARE,
            CompositeMatcher.createMatcherFor(
                    ItemMatcher.itemMatcherFor(49, 50, 69, 70, 89, 90),
                    RangeMatcher.rangeMatcherFor(
                            between(7, 9),
                            between(16, 18),
                            between(37, 39),
                            between(46, 48),
                            between(57, 59),
                            between(66, 68),
                            between(77, 79),
                            between(86, 88)
                    )),
            Matcher.DONT_CARE),

    POWER(MeasurementKind.POWER,
            ItemMatcher.ELECTRICITY_MATCH,
            RangeMatcher.rangeMatcherFor(
                    between(1, 10),
                    between(15, 30),
                    between(35, 50),
                    between(55, 70),
                    between(75, 80)),
            Matcher.DONT_CARE,
            ItemMatcher.itemsDontMatchFor(124),
            UnitMatcher.flowUnitMatcher()),

    POWER_FACTOR(MeasurementKind.POWERFACTOR,
            ItemMatcher.ELECTRICITY_MATCH,
            ItemMatcher.itemMatcherFor(13, 33, 53, 73),
            Matcher.DONT_CARE,
            Matcher.DONT_CARE,
            Matcher.DONT_CARE),

    VOLTAGE_SAG(MeasurementKind.SAG,
            ItemMatcher.ELECTRICITY_MATCH,
            ItemMatcher.itemsDontMatchFor(0, 93, 94, 96, 97, 98, 99),
            CompositeMatcher.createMatcherFor(
                    ItemMatcher.itemMatcherFor(43),
                    RangeMatcher.rangeMatcherFor(between(31, 34))),
            Matcher.DONT_CARE,
            Matcher.DONT_CARE),

    VOLTAGE_SWELL(MeasurementKind.SWELL,
            ItemMatcher.ELECTRICITY_MATCH,
            ItemMatcher.itemsDontMatchFor(0, 93, 94, 96, 97, 98, 99),
            CompositeMatcher.createMatcherFor(
                    ItemMatcher.itemMatcherFor(44),
                    RangeMatcher.rangeMatcherFor(between(35, 38))),
            Matcher.DONT_CARE,
            Matcher.DONT_CARE),

    TOTAL_HARMONIC_DISTORTION(MeasurementKind.TOTALHARMONICDISTORTION,
            ItemMatcher.ELECTRICITY_MATCH,
            ItemMatcher.itemMatcherFor(12, 32, 52, 72, 92, 11, 31, 51, 71, 90, 91, 15, 35, 55, 75),
            ItemMatcher.itemMatcherFor(7, 24),
            ItemMatcher.itemMatcherFor(124),
            Matcher.DONT_CARE),

    TRANSFORMER_LOSSES(MeasurementKind.TRANSFORMERLOSSES,
            ItemMatcher.ELECTRICITY_MATCH,
            ItemMatcher.itemMatcherFor(83),
            Matcher.DONT_CARE,
            CompositeMatcher.createMatcherFor(
                    ItemMatcher.itemMatcherFor(19),
                    RangeMatcher.rangeMatcherFor(
                            between(4, 6),
                            between(13, 15),
                            between(34, 36),
                            between(43, 45),
                            between(54, 56),
                            between(63, 65),
                            between(74, 76),
                            between(83, 85))),
            Matcher.DONT_CARE),

    //    UNIPEDE_V_DIP_1015(), //TODO DLMS Cosem has devided these based on delta time ...
//    UNIPEDE_V_DIP_1530(), //TODO DLMS Cosem has devided these based on delta time ...
//    UNIPEDE_V_DIP_3060(), //TODO DLMS Cosem has devided these based on delta time ...
//    UNIPEDE_V_DIP_6090(), //TODO DLMS Cosem has devided these based on delta time ...
//    UNIPEDE_V_DIP_90100(), //TODO DLMS Cosem has devided these based on delta time ...

    VOLTAGE(MeasurementKind.VOLTAGE,
            ItemMatcher.ELECTRICITY_MATCH,
            ItemMatcher.itemMatcherFor(12, 32, 52, 72, 92),
            ItemMatcher.itemMatcherFor(7),  // assuming only instantaneous values
            ItemMatcher.itemsDontMatchFor(124),
            Matcher.DONT_CARE),

    VOLTAGE_ANGLE(MeasurementKind.VOLTAGEANGLE,
            ItemMatcher.ELECTRICITY_MATCH,
            ItemMatcher.itemMatcherFor(81),
            ItemMatcher.itemMatcherFor(7),
            RangeMatcher.rangeMatcherFor(
                    between(0, 2),
                    between(10, 12),
                    between(20, 22),
                    between(30, 32),
                    between(40, 42),
                    between(50, 52),
                    between(60, 62),
                    between(70, 72)),
            Matcher.DONT_CARE),

    VOLUME(MeasurementKind.VOLUME,
            ItemMatcher.GAS_MATCH,
            Matcher.DONT_CARE,
            Matcher.DONT_CARE,
            Matcher.DONT_CARE,
            Matcher.DONT_CARE);

    private final MeasurementKind kind;
    private final Matcher<Integer> aField;
    private final Matcher<Integer> cField;
    private final Matcher<Integer> dField;
    private final Matcher<Integer> eField;
    private final Matcher<Integer> unitMatcher;

    MeasurementKindMapping(MeasurementKind kind, Matcher<Integer> aField, Matcher<Integer> cField, Matcher<Integer> dField, Matcher<Integer> eField, Matcher<Integer> unitMatcher) {
        this.kind = kind;
        this.aField = aField;
        this.cField = cField;
        this.dField = dField;
        this.eField = eField;
        this.unitMatcher = unitMatcher;
    }

    public static final Range between(int from, int to) {
        return new Range(from, to);
    }

    /**
     * Returns the MeasurementKind which corresponds with the provided ObisCode
     *
     * @param obisCode the provided ObisCode
     * @return the corresponding {@link MeasurementKind}
     *         or {@link MeasurementKind#NOTAPPLICABLE} if no match was found
     */
    public static MeasurementKind getMeasurementKindFor(ObisCode obisCode, Unit unit) {
        if (obisCode != null && unit != null) {
            for (MeasurementKindMapping mkm : values()) {
                if (mkm.aField.match(obisCode.getA()) &&
                        mkm.cField.match(obisCode.getC()) &&
                        mkm.dField.match(obisCode.getD()) &&
                        mkm.eField.match(obisCode.getE()) &&
                        mkm.unitMatcher.match(unit.getDlmsCode())) {
                    return mkm.kind;
                }
            }
        }
        return MeasurementKind.NOTAPPLICABLE;
    }

    MeasurementKind getKind() {
        return kind;
    }

    Matcher<Integer> getaField() {
        return aField;
    }

    Matcher<Integer> getcField() {
        return cField;
    }

    Matcher<Integer> getdField() {
        return dField;
    }

    Matcher<Integer> geteField() {
        return eField;
    }

    Matcher<Integer> getUnitMatcher() {
        return unitMatcher;
    }
}
