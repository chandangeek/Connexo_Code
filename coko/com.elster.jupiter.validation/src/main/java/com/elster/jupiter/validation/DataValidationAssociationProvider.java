package com.elster.jupiter.validation;

import com.elster.jupiter.metering.EndDevice;

import aQute.bnd.annotation.ConsumerType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;

/**
 * Created by albertv on 6/29/2016.
 */
@ConsumerType
public interface DataValidationAssociationProvider {

    List<DataValidationStatus> getRegisterSuspects(EndDevice endDevice, Range<Instant> range);

    List<DataValidationStatus> getChannelsSuspects(EndDevice endDevice, Range<Instant> range);

    boolean isAllDataValidated(EndDevice endDevice, Range<Instant> range);
}
