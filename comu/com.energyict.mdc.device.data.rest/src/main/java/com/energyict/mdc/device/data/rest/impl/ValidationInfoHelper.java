package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.util.streams.Functions;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.DataValidationStatus;
import com.energyict.mdc.device.data.DeviceValidation;
import com.energyict.mdc.device.data.Reading;
import com.energyict.mdc.device.data.Register;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ValidationInfoHelper {
    private final Clock clock;
    private final ValidationInfoFactory validationInfoFactory;

    @Inject
    public ValidationInfoHelper(Clock clock, ValidationInfoFactory validationInfoFactory) {
        this.clock = clock;
        this.validationInfoFactory = validationInfoFactory;
    }

    public DetailedValidationInfo getRegisterValidationInfo(Register<?> register) {
        boolean validationActive = validationActive(register.getDevice().forValidation());

        Optional<Instant> lastChecked = register.getDevice().forValidation().getLastChecked(register);
        return validationInfoFactory.createDetailedValidationInfo(validationActive, statuses(register), lastChecked);
    }

    private List<DataValidationStatus> statuses(Register<?> register) {
        List<? extends Reading> readings = getReadingsForOneYear(register);
        return readings.stream().map(Reading::getValidationStatus).flatMap(Functions.asStream()).collect(Collectors.toList());
    }

    private boolean validationActive(DeviceValidation deviceValidation) {
        return deviceValidation.isValidationActive();
    }

    private List<? extends Reading> getReadingsForOneYear(Register<?> register) {
        return register.getReadings(lastYear());
    }

    private Interval lastYear() {
        ZonedDateTime end = clock.instant().atZone(ZoneId.of("UTC")).with(ChronoField.MILLI_OF_DAY, 0L).plusDays(1);
        ZonedDateTime start = end.minusYears(1);
        return Interval.of(start.toInstant(), end.toInstant());
    }

}