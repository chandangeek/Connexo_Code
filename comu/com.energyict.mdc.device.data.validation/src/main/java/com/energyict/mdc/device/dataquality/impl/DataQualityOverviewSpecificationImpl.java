/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.dataquality.impl;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.energyict.mdc.device.config.DeviceType;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

class DataQualityOverviewSpecificationImpl {

    private final List<KpiType> availableKpiTypes;

    private Set<EndDeviceGroup> deviceGroups = new HashSet<>();

    private Set<DeviceType> deviceTypes = new HashSet<>();

    private Range<Instant> period = Range.all();

    private Set<KpiType> readingQualityTypes = new HashSet<>();

    private Set<KpiType> validators = new HashSet<>();

    private Set<KpiType> estimators = new HashSet<>();

    private MetricValueRange amountOfSuspects;
    private MetricValueRange amountOfConfirmed;
    private MetricValueRange amountOfEstimates;
    private MetricValueRange amountOfInformatives;
    private MetricValueRange amountOfEdited;

    private int from;
    private int to;

    DataQualityOverviewSpecificationImpl(List<KpiType> availableKpiTypes) {
        this.availableKpiTypes = availableKpiTypes;
    }

    void addDeviceGroups(Collection<EndDeviceGroup> deviceGroups) {
        this.deviceGroups.addAll(deviceGroups);
    }

    void addDeviceTypes(Collection<DeviceType> deviceTypes) {
        this.deviceTypes.addAll(deviceTypes);
    }

    void setPeriod(Range<Instant> period) {
        this.period = period;
    }

    void addReadingQualityTypes(Collection<KpiType> readingQualityTypes) {
        this.readingQualityTypes.addAll(readingQualityTypes);
    }

    void addValidators(Collection<KpiType> validators) {
        this.validators.addAll(validators);
    }

    void addEstimators(Collection<KpiType> estimators) {
        this.estimators.addAll(estimators);
    }

    void setAmountOfSuspects(long amountOfSuspects) {
        this.amountOfSuspects = new MetricValueRange.ExactMatch(amountOfSuspects);
    }

    void setAmountOfSuspects(Range<Long> range) {
        this.amountOfSuspects = new MetricValueRange.LongRange(range);
    }

    void setAmountOfConfirmed(long amountOfConfirmed) {
        this.amountOfConfirmed = new MetricValueRange.ExactMatch(amountOfConfirmed);
    }

    void setAmountOfConfirmed(Range<Long> range) {
        this.amountOfConfirmed = new MetricValueRange.LongRange(range);
    }

    void setAmountOfEstimates(long amountOfEstimates) {
        this.amountOfEstimates = new MetricValueRange.ExactMatch(amountOfEstimates);
    }

    void setAmountOfEstimates(Range<Long> range) {
        this.amountOfEstimates = new MetricValueRange.LongRange(range);
    }

    void setAmountOfInformatives(long amountOfInformatives) {
        this.amountOfInformatives = new MetricValueRange.ExactMatch(amountOfInformatives);
    }

    void setAmountOfInformatives(Range<Long> range) {
        this.amountOfInformatives = new MetricValueRange.LongRange(range);
    }

    void setAmountOfEdited(long amountOfEdited) {
        this.amountOfEdited = new MetricValueRange.ExactMatch(amountOfEdited);
    }

    void setAmountOfEdited(Range<Long> range) {
        this.amountOfEdited = new MetricValueRange.LongRange(range);
    }

    void paged(int from, int to) {
        this.from = from;
        this.to = to;
    }

    Set<EndDeviceGroup> getDeviceGroups() {
        return Collections.unmodifiableSet(deviceGroups);
    }

    Set<DeviceType> getDeviceTypes() {
        return Collections.unmodifiableSet(deviceTypes);
    }

    Range<Instant> getPeriod() {
        return period;
    }

    Set<KpiType> getReadingQualityTypes() {
        return Collections.unmodifiableSet(readingQualityTypes);
    }

    Set<KpiType> getValidators() {
        return Collections.unmodifiableSet(validators);
    }

    Set<KpiType> getEstimators() {
        return Collections.unmodifiableSet(estimators);
    }

    Optional<MetricValueRange> getAmountOfSuspects() {
        return Optional.ofNullable(amountOfSuspects);
    }

    Optional<MetricValueRange> getAmountOfConfirmed() {
        return Optional.ofNullable(amountOfConfirmed);
    }

    Optional<MetricValueRange> getAmountOfEstimates() {
        return Optional.ofNullable(amountOfEstimates);
    }

    Optional<MetricValueRange> getAmountOfInformatives() {
        return Optional.ofNullable(amountOfInformatives);
    }

    Optional<MetricValueRange> getAmountOfEdited() {
        return Optional.ofNullable(amountOfEdited);
    }

    int getFrom() {
        return from;
    }

    int getTo() {
        return to;
    }

    List<KpiType> getAvailableKpiTypes() {
        return Collections.unmodifiableList(this.availableKpiTypes);
    }
}
