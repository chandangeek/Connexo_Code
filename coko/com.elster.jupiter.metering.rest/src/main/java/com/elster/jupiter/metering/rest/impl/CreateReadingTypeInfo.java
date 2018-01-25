/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.rest.impl;

import java.util.Arrays;
import java.util.Collections;
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
        return (value == null) ? Collections.emptyList() : Collections.singletonList(value);
    }

    private static List<Integer> ensureList(List<Integer> list) {
        return (list == null) ? Collections.emptyList() : list;
    }


    static CreateReadingTypeInfo fromBasicCreateReadingTypeInfo(CreateBasicReadingTypeInfo createBasicReadingTypeInfo) {
        CreateReadingTypeInfo createReadingTypeInfo = new CreateReadingTypeInfo();

        createReadingTypeInfo.mRID = createBasicReadingTypeInfo.mRID;
        createReadingTypeInfo.aliasName = createBasicReadingTypeInfo.aliasName;

        // Interval < day is not a valid period value. We use 0 when we add the reading type
        if (intervalLessThanDayPeriod(createBasicReadingTypeInfo.basicMacroPeriod)){
            createReadingTypeInfo.macroPeriod = Collections.singletonList(0);
        } else {
            createReadingTypeInfo.macroPeriod = makeList(createBasicReadingTypeInfo.basicMacroPeriod);
        }

        createReadingTypeInfo.aggregate = makeList(createBasicReadingTypeInfo.basicAggregate);
        createReadingTypeInfo.measuringPeriod = makeList(createBasicReadingTypeInfo.basicMeasuringPeriod);
        createReadingTypeInfo.accumulation = makeList(createBasicReadingTypeInfo.basicAccumulation);
        createReadingTypeInfo.flowDirection = makeList(createBasicReadingTypeInfo.basicFlowDirection);

        // BasicCommodity combo only has the Primary Electricity value available. If that is selected,
        // we will create both primary and secondary.
        if (primaryElectricity(createBasicReadingTypeInfo.basicCommodity)){
            createReadingTypeInfo.commodity = Arrays.asList(1,2);
        } else {
            makeList(createBasicReadingTypeInfo.basicCommodity);
        }

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

    private static boolean primaryElectricity(Integer basicCommodity) {
        return basicCommodity != null && basicCommodity == 2;
    }

    private static boolean intervalLessThanDayPeriod(Integer basicMeasuringPeriod) {
        return basicMeasuringPeriod != null && basicMeasuringPeriod == 0x10000;
    }

    void fixNullLists() {
        macroPeriod = ensureList(macroPeriod);
        aggregate = ensureList(aggregate);
        measuringPeriod = ensureList(measuringPeriod);
        accumulation = ensureList(accumulation);
        flowDirection = ensureList(flowDirection);
        commodity = ensureList(commodity);


        measurementKind = ensureList(measurementKind);
        interHarmonicNumerator = ensureList(interHarmonicNumerator);
        interHarmonicDenominator = ensureList(interHarmonicDenominator);
        argumentNumerator = ensureList(argumentNumerator);
        argumentDenominator = ensureList(argumentDenominator);
        tou = ensureList(tou);
        cpp = ensureList(cpp);

        consumptionTier = ensureList(consumptionTier);
        phases = ensureList(phases);
        metricMultiplier = ensureList(metricMultiplier);
        currency = ensureList(currency);
        unit = ensureList(unit);

    }
}
