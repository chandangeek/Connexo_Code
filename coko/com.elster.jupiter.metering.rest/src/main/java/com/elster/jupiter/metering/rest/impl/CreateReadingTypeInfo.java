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

    private static List<Integer> makeList(Integer value) {
        return (value == null) ? Arrays.asList() : Arrays.asList(value);
    }

    public static CreateReadingTypeInfo fromBasicCreateReadingTypeInfo(CreateBasicReadingTypeInfo createBasicReadingTypeInfo) {
        CreateReadingTypeInfo createReadingTypeInfo = new CreateReadingTypeInfo();

        createReadingTypeInfo.mRID = createBasicReadingTypeInfo.mRID;
        createReadingTypeInfo.aliasName = createBasicReadingTypeInfo.aliasName;
        createReadingTypeInfo.macroPeriod = makeList(createBasicReadingTypeInfo.basicMacroPeriod);
        createReadingTypeInfo.aggregate = makeList(createBasicReadingTypeInfo.basicAggregate);
        createReadingTypeInfo.measuringPeriod = makeList(createBasicReadingTypeInfo.basicMeasuringPeriod);
        createReadingTypeInfo.accumulation = makeList(createBasicReadingTypeInfo.basicAccumulation);
        createReadingTypeInfo.flowDirection = makeList(createBasicReadingTypeInfo.basicFlowDirection);
        createReadingTypeInfo.commodity = (createBasicReadingTypeInfo.basicCommodity != null && createBasicReadingTypeInfo.basicCommodity == 2) ?
                Arrays.asList(createBasicReadingTypeInfo.basicCommodity, 1) : Arrays.asList(); // Electricity 1 & 2
        createReadingTypeInfo.measurementKind = makeList(createBasicReadingTypeInfo.basicMeasurementKind);
        createReadingTypeInfo.interHarmonicNumerator = makeList(null);
        createReadingTypeInfo.interHarmonicDenominator = makeList(null);
        createReadingTypeInfo.argumentNumerator = makeList(null);
        createReadingTypeInfo.argumentDenominator = makeList(null);
        createReadingTypeInfo.tou = createBasicReadingTypeInfo.basicTou;
        createReadingTypeInfo.cpp = createBasicReadingTypeInfo.basicCpp;
        createReadingTypeInfo.consumptionTier = createBasicReadingTypeInfo.basicConsumptionTier;
        createReadingTypeInfo.phases = createBasicReadingTypeInfo.basicPhases;
        createReadingTypeInfo.metricMultiplier = createBasicReadingTypeInfo.basicMetricMultiplier;
        createReadingTypeInfo.unit = makeList(createBasicReadingTypeInfo.basicUnit);
        createReadingTypeInfo.currency = makeList(null);
        return createReadingTypeInfo;
    }
}
