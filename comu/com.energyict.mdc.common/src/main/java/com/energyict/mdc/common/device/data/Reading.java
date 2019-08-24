/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.device.data;

import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.validation.DataValidationStatus;

import aQute.bnd.annotation.ConsumerType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

/**
 * Models data that was read from a Device and stored in a {@link Register}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-14 (11:58)
 */
@ConsumerType
public interface Reading {

    ReadingType getType();

    Instant getTimeStamp();

    Optional<Instant> getEventDate();

    Instant getReportedDateTime();

    BigDecimal getSensorAccuracy();

    String getSource();

    ReadingRecord getActualReading();

    Optional<DataValidationStatus> getValidationStatus();

}