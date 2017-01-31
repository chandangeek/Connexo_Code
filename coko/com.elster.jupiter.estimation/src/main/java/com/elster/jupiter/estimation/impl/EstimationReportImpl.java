/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationReport;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.metering.ReadingType;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

class EstimationReportImpl implements EstimationReport {

    private Map<ReadingType, SimpleEstimationResult.EstimationResultBuilder> results = new HashMap<>();

    @Override
    public Map<ReadingType, EstimationResult> getResults() {
        return results.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().build()));
    }

    public void reportEstimated(ReadingType readingType, EstimationBlock block) {
        results.computeIfAbsent(readingType, rt -> SimpleEstimationResult.builder())
                .addEstimated(block);
    }

    public void reportUnableToEstimate(ReadingType readingType, EstimationBlock block) {
        results.computeIfAbsent(readingType, rt -> SimpleEstimationResult.builder())
                .addRemaining(block);
    }

    public void add(EstimationReport subReport) {
        subReport.getResults().entrySet().forEach(entry -> {
            entry.getValue().estimated().forEach(block -> reportEstimated(entry.getKey(), block));
            entry.getValue().remainingToBeEstimated().forEach(block -> reportUnableToEstimate(entry.getKey(), block));
        });
    }
}
