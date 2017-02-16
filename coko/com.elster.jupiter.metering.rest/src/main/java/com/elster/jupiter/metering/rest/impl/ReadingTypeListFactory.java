/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.rest.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class ReadingTypeListFactory {
    private CreateReadingTypeInfo info;
    private List<String> codeList;

    ReadingTypeListFactory(CreateReadingTypeInfo info) {
        this.info = info;
    }

    List<String> getCodeStringList() {
        codeList = Collections.singletonList("");
        addMacroPeriod();
        addAggregate();
        addMeasurementPeriod();
        addAccumulation();
        addFlowDirection();
        addCommodity();
        addMeasurementKind();
        addInterHarmonicNumerator();
        addInterHarmonicDenominator();
        addArgumentNumerator();
        addArgumentDenominator();
        addTou();
        addCpp();
        addConsumptionTier();
        addPhases();
        addMultiplier();
        addUnit();
        addCurrency();
        return Collections.unmodifiableList(codeList);
    }

    private void addMacroPeriod() {
        this.addFirstToCodeListFrom(info.macroPeriod);
    }

    private void addAggregate() {
        this.addToCodeListFrom(info.aggregate);
    }

    private void addMeasurementPeriod() {
        this.addToCodeListFrom(info.measuringPeriod);
    }

    private void addAccumulation() {
        this.addToCodeListFrom(info.accumulation);
    }

    private void addFlowDirection() {
        this.addToCodeListFrom(info.flowDirection);
    }

    private void addCommodity() {
        this.addToCodeListFrom(info.commodity);
    }

    private void addMeasurementKind() {
        this.addToCodeListFrom(info.measurementKind);
    }

    private void addInterHarmonicNumerator() {
        this.addToCodeListFrom(info.interHarmonicNumerator);
    }

    private void addInterHarmonicDenominator() {
        this.addToCodeListFrom(info.interHarmonicDenominator);
    }

    private void addArgumentNumerator() {
        this.addToCodeListFrom(info.argumentNumerator);
    }

    private void addArgumentDenominator() {
        this.addToCodeListFrom(info.argumentDenominator);
    }

    private void addTou() {
        this.addToCodeListFrom(info.tou);
    }

    private void addCpp() {
        this.addToCodeListFrom(info.cpp);
    }

    private void addConsumptionTier() {
        this.addToCodeListFrom(info.consumptionTier);
    }

    private void addPhases() {
        this.addToCodeListFrom(info.phases);
    }

    private void addMultiplier() {
        this.addToCodeListFrom(info.metricMultiplier);
    }

    private void addUnit() {
        this.addToCodeListFrom(info.unit);
    }

    private void addCurrency() {
        this.addToCodeListFrom(info.currency);
    }

    private void addToCodeListFrom(List<Integer> numbers) {
        List<String> tempList = new ArrayList<>();
        if (numbers.isEmpty()) {
            this.codeList.stream().map(e -> e + ".0").forEach(tempList::add);
        }

        for (int i = 0; i < numbers.size(); i++) {
            int j = i;
            this.codeList.stream().map(e -> e + "." + numbers.get(j)).forEach(tempList::add);
        }
        this.codeList = tempList;
    }

    private void addFirstToCodeListFrom(List<Integer> numbers) {
        List<String> tempList = new ArrayList<>();
        if (numbers.isEmpty()) {
            this.codeList.stream().map(e -> e + "0").forEach(tempList::add);
        }

        for (int i = 0; i < numbers.size(); i++) {
            int j = i;
            this.codeList.stream().map(e -> e + numbers.get(j)).forEach(tempList::add);
        }
        this.codeList = tempList;
    }

}