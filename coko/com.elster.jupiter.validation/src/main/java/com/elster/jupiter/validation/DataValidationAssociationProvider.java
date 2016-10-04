package com.elster.jupiter.validation;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Created by albertv on 6/29/2016.
 */
public interface DataValidationAssociationProvider {

    List<DataValidationStatus> getRegisterSuspects(long endDeviceId, Range<Instant> range);

    List<DataValidationStatus> getChannelsSuspects(long endDeviceId, Range<Instant> range);
}
