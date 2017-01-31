/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.validation;

import com.elster.jupiter.metering.groups.EndDeviceGroup;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;

/**
 * Created by dragos on 7/21/2015.
 */
@ProviderType
public interface DeviceDataValidationService {
    String COMPONENT_NAME = "DDV";

    /**
     * Starts the building process to obtain {@link ValidationOverviews}.
     *
     * @param deviceGroups The List of {@link EndDeviceGroup}s
     * @return The {@link ValidationOverviewBuilder}
     */
    ValidationOverviewBuilder forAllGroups(List<EndDeviceGroup> deviceGroups);

    @ProviderType
    interface ValidationOverviewBuilder {
        ValidationOverviewBuilder in(Range<Instant> range);
        ValidationOverviewSuspectsSpecificationBuilder suspects();
        ValidationOverviewBuilder includeThresholdValidator();
        ValidationOverviewBuilder includeMissingValuesValidator();
        ValidationOverviewBuilder includeReadingQualitiesValidator();
        ValidationOverviewBuilder includeRegisterIncreaseValidator();
        ValidationOverviewBuilder excludeAllValidators();
        ValidationOverviewBuilder includeAllValidators();

        ValidationOverviews paged(int from, int to);
    }

    @ProviderType
    interface ValidationOverviewSuspectsSpecificationBuilder {
        ValidationOverviewBuilder equalTo(long numberOfSuspects);
        ValidationOverviewBuilder inRange(Range<Long> range);
    }

}