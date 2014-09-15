package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.*;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.ChannelValidation;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.MeterActivationValidation;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.Reading;
import com.energyict.mdc.device.data.Register;
import com.google.common.base.Optional;

import javax.inject.Inject;
import java.util.*;

public class ValidationInfoHelper {
    private final MeteringService meteringService;
    private final ValidationService validationService;
    private final Clock clock;

    @Inject
    public ValidationInfoHelper(MeteringService meteringService, ValidationService validationService, Clock clock) {
        this.meteringService = meteringService;
        this.validationService = validationService;
        this.clock = clock;
    }

    public Meter getMeterFor(Device device) {
        Optional<AmrSystem> amrSystemRef = meteringService.findAmrSystem(1);
        Optional<Meter> meterRef = amrSystemRef.get().findMeter(String.valueOf(device.getId()));
        if (!meterRef.isPresent()) {
            throw new IllegalArgumentException("Validation feature on device " + device.getmRID() +
                    " wasn't configured.");
        }
        return meterRef.get();
    }

    public MeterActivation getMeterActivationFor(Channel channel, Meter meter) {
        for (MeterActivation meterActivation : getMeterActivationsMostCurrentFirst(meter)) {
            if(meterActivation.getChannels().contains(channel)) {
                return meterActivation;
            }
        }
        return null;
    }

    public List<MeterActivation> getMeterActivationsMostCurrentFirst(Meter meter) {
        List<MeterActivation> activations = new ArrayList<>(meter.getMeterActivations());
        Collections.reverse(activations);
        return activations;
    }

    public Optional<com.elster.jupiter.metering.Channel> getChannel(MeterActivation meterActivation, ReadingType readingType) {
        for (com.elster.jupiter.metering.Channel channel : meterActivation.getChannels()) {
            if (channel.getReadingTypes().contains(readingType)) {
                return Optional.of(channel);
            }
        }
        return Optional.absent();
    }

    public RegisterValidationInfo getRegisterValidationInfo(Register register) {
        Device device = register.getDevice();
        Meter meter = getMeterFor(device);
        Optional<Channel> channelRef = getRegisterChannel(register, meter);
        List<DataValidationStatus> dataValidationStatuses = new ArrayList<>();
        boolean validationStatusForRegister = channelRef.isPresent() ? getValidationStatus(channelRef.get(), meter) : false;
        Optional<Date> lastChecked = Optional.absent();
        if (validationStatusForRegister) {
            List<Reading> readings = getReadingsForOneYear(register);
            List<ReadingRecord> readingRecords = getReadingRecords(readings);
            dataValidationStatuses = validationService.getValidationStatus(channelRef.get(), readingRecords);
            MeterActivation activation = getMeterActivationFor(channelRef.get(), meter);
            lastChecked = validationService.getLastChecked(activation);
        }
        return new RegisterValidationInfo(validationStatusForRegister, dataValidationStatuses, lastChecked.isPresent() ? lastChecked.get() : null);
    }

    public Boolean getValidationStatus(Channel channel, Meter meter) {
        MeterActivation meterActivation = getMeterActivationFor(channel, meter);
        List<? extends MeterActivationValidation> activeValidations = validationService.getActiveMeterActivationValidations(meterActivation);
        return (!validationService.validationEnabled(meter) || activeValidations.isEmpty()) ? false : getValidationStatusForChannel(activeValidations, channel) ;
    }

    private Boolean getValidationStatusForChannel(List<? extends MeterActivationValidation> activeValidations, Channel channel) {
        for (MeterActivationValidation meterActivationValidation : activeValidations) {
            if (isThereChannelValidationForChannel(meterActivationValidation, channel)) {
                return true;
            }
        }
        return false;
    }

    private boolean isThereChannelValidationForChannel(MeterActivationValidation validation, Channel channel) {
        for(ChannelValidation channelValidation : validation.getChannelValidations()) {
            if (channelValidation.getChannel().equals(channel) && channelValidation.hasActiveRules()) {
                return true;
            }
        }
        return false;
    }

    private Optional<Channel> getRegisterChannel(Register register, Meter meter) {
        for (MeterActivation meterActivation : getMeterActivationsMostCurrentFirst(meter)) {
            Optional<Channel> channelRef = getChannel(meterActivation, register.getRegisterSpec().getRegisterType().getReadingType());
            if(channelRef.isPresent()) {
                return channelRef;
            }
        }
        return Optional.absent();
    }

    private List<Reading> getReadingsForOneYear(Register register) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(clock.now());
        cal.add(Calendar.YEAR, -1);
        return  register.getReadings(Interval.startAt(cal.getTime()));
    }

    private List<ReadingRecord> getReadingRecords(List<Reading> readings) {
        List<ReadingRecord> readingRecords = new ArrayList<>(readings.size());
        for(Reading reading : readings) {
            readingRecords.add(reading.getActualReading());
        }
        return readingRecords;
    }


}
