package com.energyict.mdc.device.data;

import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.validation.DataValidationStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Models data that was read from a Device and stored in a {@link Register}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-14 (11:58)
 */
public interface Reading {

    public ReadingType getType();

    public Instant getTimeStamp();

    public Instant getReportedDateTime();

    public BigDecimal getSensorAccuracy();

    public String getSource();

    public ReadingRecord getActualReading();

    public Optional<DataValidationStatus> getValidationStatus();

}