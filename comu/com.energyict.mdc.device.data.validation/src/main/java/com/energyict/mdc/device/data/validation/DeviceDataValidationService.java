package com.energyict.mdc.device.data.validation;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Created by dragos on 7/21/2015.
 */
public interface DeviceDataValidationService {
    String COMPONENT_NAME = "DDV";

    List<ValidationOverview> getValidationResultsOfDeviceGroup(long groupId, Optional<Integer> start, Optional<Integer> limit, Range<Instant> range);

}