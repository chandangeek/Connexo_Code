/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.validation.impl;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.energyict.mdc.device.data.validation.DataQualityOverviews;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

class ValidationOverviewSpecificationImpl {

    private List<EndDeviceGroup> deviceGroups;
    private Set<ValidationOverviewSqlBuilder.KpiType> kpiTypes;
    private Range<Instant> range = Range.all();
    private SuspectsRange suspectsRange = new SuspectsRange.IgnoreSuspectRange();
    private int from;
    private int to;

    ValidationOverviewSpecificationImpl() {
        this.excludeAllValidators();
    }

    void setRange(Range<Instant> range) {
        this.range = range;
    }

    void includeThresholdValidator() {
        this.kpiTypes.add(ValidationOverviewSqlBuilder.KpiType.THRESHOLD);
    }

    void includeMissingValuesValidator() {
        this.kpiTypes.add(ValidationOverviewSqlBuilder.KpiType.MISSING_VALUES);
    }

    void includeReadingQualitiesValidator() {
        this.kpiTypes.add(ValidationOverviewSqlBuilder.KpiType.READING_QUALITIES);
    }

    void includeRegisterIncreaseValidator() {
        this.kpiTypes.add(ValidationOverviewSqlBuilder.KpiType.REGISTER_INCREASE);
    }

    void excludeAllValidators() {
        this.kpiTypes = EnumSet.noneOf(ValidationOverviewSqlBuilder.KpiType.class);
    }

    void includeAllValidators() {
        this.kpiTypes =
                EnumSet.of(
                        ValidationOverviewSqlBuilder.KpiType.THRESHOLD,
                        ValidationOverviewSqlBuilder.KpiType.MISSING_VALUES,
                        ValidationOverviewSqlBuilder.KpiType.READING_QUALITIES,
                        ValidationOverviewSqlBuilder.KpiType.REGISTER_INCREASE);
    }

    void setNumberOfSuspects(long numberOfSuspects) {
        this.suspectsRange = new SuspectsRange.ExactMatch(numberOfSuspects);
    }

    void setSuspectRange(Range<Long> range) {
        this.suspectsRange = new SuspectsRange.LongRange(range);
    }

    DataQualityOverviews paged(int from, int to) {
        this.from = from;
        this.to = to;
        return queryWith(this);
    }
}
