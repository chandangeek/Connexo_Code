/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.validation;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.energyict.mdc.device.config.DeviceType;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;

@ProviderType
public interface DeviceDataQualityService {

    String COMPONENT_NAME = "DDQ";

    /**
     * Starts the building process to obtain {@link DataQualityOverview}.
     */
    DataQualityOverviewBuilder forAllDevices();

    @ProviderType
    interface DataQualityOverviewBuilder {

        DataQualityOverviewBuilder in(List<EndDeviceGroup> deviceGroups);

        DataQualityOverviewBuilder of(DeviceType deviceType);

        DataQualityOverviewBuilder in(Range<Instant> range);

        MetricSpecificationBuilder hasSuspects();

        MetricSpecificationBuilder hasConfirmed();

        MetricSpecificationBuilder hasEstimated();

        MetricSpecificationBuilder hasInformatives();

        MetricSpecificationBuilder hasEdited();

        MetricSpecificationBuilder suspects();

        MetricSpecificationBuilder confirmed();

        MetricSpecificationBuilder estimated();

        MetricSpecificationBuilder informatives();

        MetricSpecificationBuilder edited();

        DataQualityOverviews paged(int from, int to);
    }

    @ProviderType
    interface MetricSpecificationBuilder {

        DataQualityOverviewBuilder equalTo(long numberOfSuspects);

        DataQualityOverviewBuilder inRange(Range<Long> range);

        DataQualityOverviewBuilder greaterThan(long numberOfSuspects);

        DataQualityOverviewBuilder lessThan(long numberOfSuspects);
    }
}
