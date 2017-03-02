/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.dataquality;

import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.validation.Validator;
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

        DataQualityOverviewBuilder of(List<DeviceType> deviceTypes);

        DataQualityOverviewBuilder in(Range<Instant> range);

        DataQualityOverviewBuilder havingSuspects();

        DataQualityOverviewBuilder suspectedBy(List<Validator> validators);

        DataQualityOverviewBuilder havingEstimates();

        DataQualityOverviewBuilder estimatedBy(List<Estimator> estimators);

        DataQualityOverviewBuilder havingEdited();

        DataQualityOverviewBuilder havingConfirmed();

        DataQualityOverviewBuilder havingInformatives();

        MetricSpecificationBuilder suspects();

        MetricSpecificationBuilder confirmed();

        MetricSpecificationBuilder estimates();

        MetricSpecificationBuilder informatives();

        MetricSpecificationBuilder edited();

        DataQualityOverviews paged(int from, int to);
    }

    @ProviderType
    interface MetricSpecificationBuilder {

        DataQualityOverviewBuilder equalTo(long value);

        DataQualityOverviewBuilder inRange(Range<Long> range);
    }
}
