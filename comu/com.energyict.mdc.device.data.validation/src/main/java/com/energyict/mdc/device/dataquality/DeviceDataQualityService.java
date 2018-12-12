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
import java.util.Collection;

@ProviderType
public interface DeviceDataQualityService {

    String COMPONENT_NAME = "DDQ";

    /**
     * Starts the building process to obtain {@link DataQualityOverview}.
     */
    DataQualityOverviewBuilder forAllDevices();

    @ProviderType
    interface DataQualityOverviewBuilder {

        DataQualityOverviewBuilder in(Collection<EndDeviceGroup> deviceGroups);

        DataQualityOverviewBuilder of(Collection<DeviceType> deviceTypes);

        DataQualityOverviewBuilder in(Range<Instant> range);

        DataQualityOverviewBuilder having(Collection<ReadingQualityType> readingQualityTypes);

        DataQualityOverviewBuilder havingSuspectsBy(Collection<Validator> validators);

        DataQualityOverviewBuilder havingEstimatesBy(Collection<Estimator> estimators);

        MetricSpecificationBuilder withSuspectsAmount();

        MetricSpecificationBuilder withConfirmedAmount();

        MetricSpecificationBuilder withEstimatesAmount();

        MetricSpecificationBuilder withInformativesAmount();

        MetricSpecificationBuilder withEditedAmount();

        DataQualityOverviews paged(int from, int to);

    }

    enum ReadingQualityType {
        SUSPECTS,
        INFORMATIVES,
        ESTIMATES,
        EDITED,
        CONFIRMED
    }

    @ProviderType
    interface MetricSpecificationBuilder {

        DataQualityOverviewBuilder equalTo(long value);

        DataQualityOverviewBuilder inRange(Range<Long> range);
    }
}
