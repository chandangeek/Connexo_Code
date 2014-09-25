package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.*;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.device.data.*;
import com.google.common.base.Optional;

import javax.inject.Inject;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class ValidationInfoHelper {
    private final ValidationService validationService;
    private final Clock clock;
    private final ResourceHelper resourceHelper;

    @Inject
    public ValidationInfoHelper(ValidationService validationService, Clock clock, ResourceHelper resourceHelper) {
        this.validationService = validationService;
        this.clock = clock;
        this.resourceHelper = resourceHelper;
    }

    public DetailedValidationInfo getRegisterValidationInfo(Register register) {
        Device device = register.getDevice();
        Meter meter = resourceHelper.getMeterFor(device);
        Optional<Channel> channelRef = resourceHelper.getRegisterChannel(register, meter);
        List<DataValidationStatus> dataValidationStatuses = new ArrayList<>();
        boolean validationStatusForRegister = getValidationStatus(channelRef, meter);
        Optional<Date> lastChecked = Optional.absent();
        if(channelRef.isPresent()) {
            List<Reading> readings = getReadingsForOneYear(register);
            List<ReadingRecord> readingRecords = readings.stream().map(r -> r.getActualReading()).collect(Collectors.toList());
            dataValidationStatuses = validationService.getEvaluator().getValidationStatus(channelRef.get(), readingRecords);
            lastChecked = validationService.getLastChecked(channelRef.get());
        }
        return new DetailedValidationInfo(validationStatusForRegister, dataValidationStatuses, lastChecked.orNull());
    }

    public Boolean getValidationStatus(Optional<Channel> channelRef, Meter meter) {
        return channelRef.isPresent() && validationService.validationEnabled(meter) && validationService.isValidationActive(channelRef.get());
    }

    private List<Reading> getReadingsForOneYear(Register register) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(clock.now());
        cal.add(Calendar.YEAR, -1);
        return  register.getReadings(Interval.startAt(cal.getTime()));
    }
}
