/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.rest.impl;

import java.util.Arrays;
import java.util.List;

public class CreateReadingTypeInfo {
    public String mRID;
    public String aliasName;
    public List<Integer> macroPeriod;
    public List<Integer> aggregate;
    public List<Integer> measuringPeriod;
    public List<Integer> accumulation;
    public List<Integer> flowDirection;
    public List<Integer> commodity;
    public List<Integer> measurementKind;
    public List<Integer> interHarmonicNumerator;
    public List<Integer> interHarmonicDenominator;
    public List<Integer> argumentNumerator;
    public List<Integer> argumentDenominator;
    public List<Integer> tou;
    public List<Integer> cpp;
    public List<Integer> consumptionTier;
    public List<Integer> phases;
    public List<Integer> metricMultiplier;
    public List<Integer> unit;
    public List<Integer> currency;

    public CreateReadingTypeInfo() {
    }

    public long countVariations() {
        long[] sizes = {macroPeriod.size()
                , aggregate.size()
                , measuringPeriod.size()
                , accumulation.size()
                , flowDirection.size()
                , commodity.size()
                , measurementKind.size()
                , interHarmonicNumerator.size()
                , interHarmonicDenominator.size()
                , argumentNumerator.size()
                , argumentDenominator.size()
                , tou.size()
                , cpp.size()
                , consumptionTier.size()
                , phases.size()
                , metricMultiplier.size()
                , unit.size()
                , currency.size()};
        return Arrays.stream(sizes).filter(e -> e > 0).reduce((a, b) -> a * b).getAsLong();
    }
}
