package com.elster.jupiter.validation;

import aQute.bnd.annotation.ConsumerType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;

/**
 * Created by albertv on 6/29/2016.
 */
@ConsumerType
public interface DataValidationAssociationProvider {

    List<DataValidationStatus> getRegisterSuspects(String mRID, Range<Instant> range);

    List<DataValidationStatus> getChannelsSuspects(String mRID, Range<Instant> range);

    boolean isAllDataValidated(String mRID, Range<Instant> range);
}
