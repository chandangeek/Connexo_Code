/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.validation.impl;

import com.energyict.mdc.device.data.validation.DataQualityOverviews;
import com.energyict.mdc.device.data.validation.DeviceDataQualityService;

import com.google.common.collect.Range;

import java.time.Instant;

class DataQualityOverviewBuilderImpl implements DeviceDataQualityService.DataQualityOverviewBuilder {

    private final ValidationOverviewSpecificationImpl underConstruction;

    DataQualityOverviewBuilderImpl() {
        super();
        this.underConstruction = new ValidationOverviewSpecificationImpl();
    }

    @Override
    public DeviceDataQualityService.DataQualityOverviewBuilder in(Range<Instant> range) {
        this.underConstruction.setRange(range);
        return this;
    }

    @Override
    public DeviceDataQualityService.MetricSpecificationBuilder suspects() {
        return new MetricSpecificationBuilderImpl(this);
    }

    @Override
    public DataQualityOverviews paged(int from, int to) {
        return this.underConstruction.paged(from, to);
    }

    private class MetricSpecificationBuilderImpl implements DeviceDataQualityService.MetricSpecificationBuilder {

        private final DataQualityOverviewBuilderImpl continuation;

        MetricSpecificationBuilderImpl(DataQualityOverviewBuilderImpl continuation) {
            this.continuation = continuation;
        }

        @Override
        public DeviceDataQualityService.DataQualityOverviewBuilder equalTo(long numberOfSuspects) {
            this.continuation.underConstruction.setNumberOfSuspects(numberOfSuspects);
            return this.continuation;
        }

        @Override
        public DeviceDataQualityService.DataQualityOverviewBuilder inRange(Range<Long> range) {
            this.continuation.underConstruction.setSuspectRange(range);
            return this.continuation;
        }
    }
}