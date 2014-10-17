package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.DataValidationStatus;
import com.energyict.mdc.device.data.DeviceValidation;
import com.energyict.mdc.device.data.Reading;
import com.energyict.mdc.device.data.Register;

import javax.inject.Inject;
import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class ValidationInfoHelper {
    private final Clock clock;

    @Inject
    public ValidationInfoHelper(Clock clock) {
        this.clock = clock;
    }

    public DetailedValidationInfo getRegisterValidationInfo(Register<?> register) {
        boolean validationActive = validationActive(register, register.getDevice().forValidation());
        Date lastChecked = lastChecked(register);
        return new DetailedValidationInfo(validationActive, statuses(register), lastChecked);
    }

    private Date lastChecked(Register<?> register) {
        return register.getDevice().forValidation().getLastChecked(register)
                .map(Date::from)
                .orElse(null);
    }

    private List<DataValidationStatus> statuses(Register<?> register) {
        List<? extends Reading> readings = getReadingsForOneYear(register);
        List<ReadingRecord> readingRecords = readings.stream().map(Reading::getActualReading).collect(Collectors.toList());
        return register.getDevice().forValidation().getValidationStatus(register, readingRecords);
    }

    private boolean validationActive(Register<?> register, DeviceValidation deviceValidation) {
        return deviceValidation.isValidationActive(register, clock.instant());
    }

    private List<? extends Reading> getReadingsForOneYear(Register<?> register) {
        return register.getReadings(lastYear());
    }

    private Interval lastYear() {
        ZonedDateTime end = clock.instant().atZone(ZoneId.of("UTC")).with(ChronoField.MILLI_OF_DAY, 0L).plusDays(1);
        ZonedDateTime start = end.minusYears(1);
        return new Interval(Date.from(start.toInstant()), Date.from(end.toInstant()));
    }

}