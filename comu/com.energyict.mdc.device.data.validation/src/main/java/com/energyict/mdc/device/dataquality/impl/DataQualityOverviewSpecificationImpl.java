/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.dataquality.impl;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.energyict.mdc.device.config.DeviceType;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class DataQualityOverviewSpecificationImpl {

    private final List<KpiType> availableKpiTypes;

    private Set<KpiType> enabledKpiTypes = new HashSet<>();

    private Set<EndDeviceGroup> deviceGroups = new HashSet<>();

    private Set<DeviceType> deviceTypes = new HashSet<>();

    private Range<Instant> period = Range.all();

    private MetricValueRange amountOfSuspects = new MetricValueRange.IgnoreRange();
    private MetricValueRange amountOfConfirmed = new MetricValueRange.IgnoreRange();
    private MetricValueRange amountOfEstimates = new MetricValueRange.IgnoreRange();
    private MetricValueRange amountOfInformatives = new MetricValueRange.IgnoreRange();
    private MetricValueRange amountOfEdited = new MetricValueRange.IgnoreRange();

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

    public void addKpiType(KpiType... kpiType) {
        this.enabledKpiTypes.addAll(Arrays.asList(kpiType));
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

    MetricValueRange getAmountOfSuspects() {
        return amountOfSuspects;
    }

    MetricValueRange getAmountOfConfirmed() {
        return amountOfConfirmed;
    }

    MetricValueRange getAmountOfEstimates() {
        return amountOfEstimates;
    }

    MetricValueRange getAmountOfInformatives() {
        return amountOfInformatives;
    }

    MetricValueRange getAmountOfEdited() {
        return amountOfEdited;
    }

    int getFrom() {
        return from;
    }

    int getTo() {
        return to;
    }

    Set<KpiType> getEnabledKpiTypes() {
        return Collections.unmodifiableSet(this.enabledKpiTypes);
    }

    List<KpiType> getAvailableKpiTypes() {
        return Collections.unmodifiableList(this.availableKpiTypes);
    }
}
