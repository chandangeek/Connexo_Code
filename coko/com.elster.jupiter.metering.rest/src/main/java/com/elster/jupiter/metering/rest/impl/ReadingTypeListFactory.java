package com.elster.jupiter.metering.rest.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ReadingTypeListFactory {
    private CreateReadingTypeInfo info;
    private List<String> codeList;

    public ReadingTypeListFactory(CreateReadingTypeInfo info) {
        this.info = info;
    }

    public List<String> getCodeStringList() {
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
        return codeList;
    }

    private void addMacroPeriod() {
        List<String> tempList = new ArrayList<>();
        if (info.macroPeriod.size() == 0) {
            tempList.addAll(codeList.stream().map(e -> e.concat("0")).collect(Collectors.toList()));
        }
        for (int i = 0; i < info.macroPeriod.size(); i++) {
            final int j = i;
            tempList.addAll(codeList.stream().map(e -> e.concat(String.valueOf(info.macroPeriod.get(j)))).collect(Collectors.toList()));
        }
        codeList = tempList;
    }

    private void addAggregate() {
        List<String> tempList = new ArrayList<>();
        if (info.aggregate.size() == 0) {
            tempList.addAll(codeList.stream().map(e -> e.concat(".0")).collect(Collectors.toList()));
        }
        for (int i = 0; i < info.aggregate.size(); i++) {
            final int j = i;
            tempList.addAll(codeList.stream().map(e -> e.concat("." + info.aggregate.get(j))).collect(Collectors.toList()));
        }
        codeList = tempList;
    }

    private void addMeasurementPeriod() {
        List<String> tempList = new ArrayList<>();
        if (info.measuringPeriod.size() == 0) {
            tempList.addAll(codeList.stream().map(e -> e.concat(".0")).collect(Collectors.toList()));
        }
        for (int i = 0; i < info.measuringPeriod.size(); i++) {
            final int j = i;
            tempList.addAll(codeList.stream().map(e -> e.concat("." + info.measuringPeriod.get(j))).collect(Collectors.toList()));
        }
        codeList = tempList;
    }

    private void addAccumulation() {
        List<String> tempList = new ArrayList<>();
        if (info.accumulation.size() == 0) {
            tempList.addAll(codeList.stream().map(e -> e.concat(".0")).collect(Collectors.toList()));
        }
        for (int i = 0; i < info.accumulation.size(); i++) {
            final int j = i;
            tempList.addAll(codeList.stream().map(e -> e.concat("." + info.accumulation.get(j))).collect(Collectors.toList()));
        }
        codeList = tempList;
    }

    private void addFlowDirection() {
        List<String> tempList = new ArrayList<>();
        if (info.flowDirection.size() == 0) {
            tempList.addAll(codeList.stream().map(e -> e.concat(".0")).collect(Collectors.toList()));
        }
        for (int i = 0; i < info.flowDirection.size(); i++) {
            final int j = i;
            tempList.addAll(codeList.stream().map(e -> e.concat("." + info.flowDirection.get(j))).collect(Collectors.toList()));
        }
        codeList = tempList;
    }

    private void addCommodity() {
        List<String> tempList = new ArrayList<>();
        if (info.commodity.size() == 0) {
            tempList.addAll(codeList.stream().map(e -> e.concat(".0")).collect(Collectors.toList()));
        }
        for (int i = 0; i < info.commodity.size(); i++) {
            final int j = i;
            tempList.addAll(codeList.stream().map(e -> e.concat("." + info.commodity.get(j))).collect(Collectors.toList()));
        }
        codeList = tempList;
    }

    private void addMeasurementKind() {
        List<String> tempList = new ArrayList<>();
        if (info.measurementKind.size() == 0) {
            tempList.addAll(codeList.stream().map(e -> e.concat(".0")).collect(Collectors.toList()));
        }
        for (int i = 0; i < info.measurementKind.size(); i++) {
            final int j = i;
            tempList.addAll(codeList.stream().map(e -> e.concat("." + info.measurementKind.get(j))).collect(Collectors.toList()));
        }
        codeList = tempList;
    }

    private void addInterHarmonicNumerator() {
        List<String> tempList = new ArrayList<>();
        if (info.interHarmonicNumerator.size() == 0) {
            tempList.addAll(codeList.stream().map(e -> e.concat(".0")).collect(Collectors.toList()));
        }
        for (int i = 0; i < info.interHarmonicNumerator.size(); i++) {
            final int j = i;
            tempList.addAll(codeList.stream().map(e -> e.concat("." + info.interHarmonicNumerator.get(j))).collect(Collectors.toList()));
        }
        codeList = tempList;
    }

    private void addInterHarmonicDenominator() {
        List<String> tempList = new ArrayList<>();
        if (info.interHarmonicDenominator.size() == 0) {
            tempList.addAll(codeList.stream().map(e -> e.concat(".0")).collect(Collectors.toList()));
        }
        for (int i = 0; i < info.interHarmonicDenominator.size(); i++) {
            final int j = i;
            tempList.addAll(codeList.stream().map(e -> e.concat("." + info.interHarmonicDenominator.get(j))).collect(Collectors.toList()));
        }
        codeList = tempList;
    }

    private void addArgumentNumerator() {
        List<String> tempList = new ArrayList<>();
        if (info.argumentNumerator.size() == 0) {
            tempList.addAll(codeList.stream().map(e -> e.concat(".0")).collect(Collectors.toList()));
        }
        for (int i = 0; i < info.argumentNumerator.size(); i++) {
            final int j = i;
            tempList.addAll(codeList.stream().map(e -> e.concat("." + info.argumentNumerator.get(j))).collect(Collectors.toList()));
        }
        codeList = tempList;
    }

    private void addArgumentDenominator() {
        List<String> tempList = new ArrayList<>();
        if (info.argumentDenominator.size() == 0) {
            tempList.addAll(codeList.stream().map(e -> e.concat(".0")).collect(Collectors.toList()));
        }
        for (int i = 0; i < info.argumentDenominator.size(); i++) {
            final int j = i;
            tempList.addAll(codeList.stream().map(e -> e.concat("." + info.argumentDenominator.get(j))).collect(Collectors.toList()));
        }
        codeList = tempList;
    }

    private void addTou() {
        List<String> tempList = new ArrayList<>();
        if (info.tou.size() == 0) {
            tempList.addAll(codeList.stream().map(e -> e.concat(".0")).collect(Collectors.toList()));
        }
        for (int i = 0; i < info.tou.size(); i++) {
            final int j = i;
            tempList.addAll(codeList.stream().map(e -> e.concat("." + info.tou.get(j))).collect(Collectors.toList()));
        }
        codeList = tempList;
    }

    private void addCpp() {
        List<String> tempList = new ArrayList<>();
        if (info.cpp.size() == 0) {
            tempList.addAll(codeList.stream().map(e -> e.concat(".0")).collect(Collectors.toList()));
        }
        for (int i = 0; i < info.cpp.size(); i++) {
            final int j = i;
            tempList.addAll(codeList.stream().map(e -> e.concat("." + info.cpp.get(j))).collect(Collectors.toList()));
        }
        codeList = tempList;
    }

    private void addConsumptionTier() {
        List<String> tempList = new ArrayList<>();
        if (info.consumptionTier.size() == 0) {
            tempList.addAll(codeList.stream().map(e -> e.concat(".0")).collect(Collectors.toList()));
        }
        for (int i = 0; i < info.consumptionTier.size(); i++) {
            final int j = i;
            tempList.addAll(codeList.stream().map(e -> e.concat("." + info.consumptionTier.get(j))).collect(Collectors.toList()));
        }
        codeList = tempList;
    }

    private void addPhases() {
        List<String> tempList = new ArrayList<>();
        if (info.phases.size() == 0) {
            tempList.addAll(codeList.stream().map(e -> e.concat(".0")).collect(Collectors.toList()));
        }
        for (int i = 0; i < info.phases.size(); i++) {
            final int j = i;
            tempList.addAll(codeList.stream().map(e -> e.concat("." + info.phases.get(j))).collect(Collectors.toList()));
        }
        codeList = tempList;
    }

    private void addMultiplier() {
        List<String> tempList = new ArrayList<>();
        if (info.metricMultiplier.size() == 0) {
            tempList.addAll(codeList.stream().map(e -> e.concat(".0")).collect(Collectors.toList()));
        }
        for (int i = 0; i < info.metricMultiplier.size(); i++) {
            final int j = i;
            tempList.addAll(codeList.stream().map(e -> e.concat("." + info.metricMultiplier.get(j))).collect(Collectors.toList()));
        }
        codeList = tempList;
    }

    private void addUnit() {
        List<String> tempList = new ArrayList<>();
        if (info.unit.size() == 0) {
            tempList.addAll(codeList.stream().map(e -> e.concat(".0")).collect(Collectors.toList()));
        }
        for (int i = 0; i < info.unit.size(); i++) {
            final int j = i;
            tempList.addAll(codeList.stream().map(e -> e.concat("." + info.unit.get(j))).collect(Collectors.toList()));
        }
        codeList = tempList;
    }

    private void addCurrency() {
        List<String> tempList = new ArrayList<>();
        if (info.currency.size() == 0) {
            tempList.addAll(codeList.stream().map(e -> e.concat(".0")).collect(Collectors.toList()));
        }
        for (int i = 0; i < info.currency.size(); i++) {
            final int j = i;
            tempList.addAll(codeList.stream().map(e -> e.concat("." + info.currency.get(j))).collect(Collectors.toList()));
        }
        codeList = tempList;
    }
}
