/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.ReadingQualityFetcher;
import com.elster.jupiter.metering.ReadingQualityIndexFilter;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityTypeFilter;
import com.elster.jupiter.metering.ReadingQualityWithTypeFetcher;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

class ReadingQualityWithTypeFetcherImpl implements ReadingQualityWithTypeFetcher {
    private static final String ANY_INDEX = "\\d+\\.\\d+";
    private String systemsRegexp = "";
    private String indicesRegexp = "";
    private String compoundIndicesRegexp = "";
    private String readingQualityTypeRegexp = "^(";
    private ReadingQualityFetcherImpl filter;

    ReadingQualityWithTypeFetcherImpl(ReadingQualityFetcherImpl filter) {
        this.filter = filter;
    }

    @Override
    public ReadingQualityWithTypeFetcher ofQualitySystems(Set<QualityCodeSystem> systems) {
        this.systemsRegexp = systems.stream()
                .map(system -> Integer.toString(system.ordinal()))
                .collect(Collectors.joining("|"));
        return this;
    }

    @Override
    public ReadingQualityWithTypeFetcher ofQualityIndices(Set<QualityCodeIndex> indices) {
        this.indicesRegexp = indices.stream()
                .map(index -> Integer.toString(index.category().ordinal()) + "\\." + Integer.toString(index.index()))
                .collect(Collectors.joining("|"));
        return this;
    }

    @Override
    public ReadingQualityWithTypeFetcher ofQualityIndices(QualityCodeCategory category, Set<Integer> indices) {
        this.indicesRegexp = Integer.toString(category.ordinal()) + "\\." +
                (indices.isEmpty() ? "\\d+" : "(" + indices.stream()
                        .map(index -> Integer.toString(index))
                        .collect(Collectors.joining("|")) + ")");
        return this;
    }

    @Override
    public ReadingQualityWithTypeFetcher ofAnyQualityIndexInCategories(Set<QualityCodeCategory> categories) {
        this.indicesRegexp = categories.stream()
                .map(category -> Integer.toString(category.ordinal()) + "\\.\\d+")
                .collect(Collectors.joining("|"));
        return this;
    }

    @Override
    public ReadingQualityWithTypeFetcher ofQualitySystem(QualityCodeSystem system) {
        this.systemsRegexp = Integer.toString(system.ordinal());
        return this;
    }

    @Override
    public ReadingQualityWithTypeFetcher ofQualityIndex(QualityCodeIndex index) {
        this.indicesRegexp = Integer.toString(index.category().ordinal()) + "\\." + Integer.toString(index.index());
        return this;
    }

    @Override
    public ReadingQualityWithTypeFetcher ofQualityIndex(QualityCodeCategory category, int index) {
        this.indicesRegexp = Integer.toString(category.ordinal()) + "\\." + Integer.toString(index);
        return this;
    }

    @Override
    public ReadingQualityWithTypeFetcher ofAnyQualityIndexInCategory(QualityCodeCategory category) {
        this.indicesRegexp = Integer.toString(category.ordinal()) + "\\.\\d+";
        return this;
    }

    @Override
    public ReadingQualityTypeFilter orOfAnotherType() {
        readingQualityTypeRegexp += buildCurrentRegexp() + "|";
        systemsRegexp = "";
        compoundIndicesRegexp = "";
        indicesRegexp = "";
        return this;
    }

    @Override
    public ReadingQualityIndexFilter orOfAnotherTypeInSameSystems() {
        compoundIndicesRegexp += buildCurrentIndicesRegexp() + "|";
        indicesRegexp = "";
        return this;
    }

    @Override
    public ReadingQualityFetcher inTimeInterval(Range<Instant> interval) {
        filter.inTimeInterval(interval);
        return this;
    }

    @Override
    public ReadingQualityFetcher atTimestamp(Instant timestamp) {
        filter.atTimestamp(timestamp);
        return this;
    }

    @Override
    public ReadingQualityFetcher actual() {
        filter.actual();
        return this;
    }

    @Override
    public ReadingQualityFetcher sorted() {
        filter.sorted();
        return this;
    }

    @Override
    public List<ReadingQualityRecord> collect() {
        finalizeRegexp();
        return filter.collect();
    }

    @Override
    public Optional<ReadingQualityRecord> findFirst() {
        finalizeRegexp();
        return filter.findFirst();
    }

    private String buildCurrentIndicesRegexp() {
        return indicesRegexp.isEmpty() ? ANY_INDEX : indicesRegexp;
    }

    private String buildCurrentRegexp() {
        // compound indices regexp is always either empty or unfinished
        compoundIndicesRegexp = compoundIndicesRegexp.isEmpty() ? indicesRegexp :
                compoundIndicesRegexp + buildCurrentIndicesRegexp();
        return (systemsRegexp.isEmpty() ? "\\d+" : "(" + systemsRegexp + ")")
                + "\\." + (compoundIndicesRegexp.isEmpty() ? ANY_INDEX : "(" + compoundIndicesRegexp + ")");
    }

    /**
     * Must be called at the very end before any terminal operation!
     */
    private void finalizeRegexp() {
        filter.ofTypeCode(readingQualityTypeRegexp + buildCurrentRegexp() + ")$");
    }
}
