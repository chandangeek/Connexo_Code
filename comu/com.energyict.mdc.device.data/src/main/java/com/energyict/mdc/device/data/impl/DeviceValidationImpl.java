package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.impl.ChannelImpl;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.ChannelValidation;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceValidation;
import com.google.common.base.Optional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by tgr on 9/09/2014.
 */
public class DeviceValidationImpl implements DeviceValidation {

    private final AmrSystem amrSystem;
    private final ValidationService validationService;
    private final DeviceImpl device;

    public DeviceValidationImpl(AmrSystem amrSystem, ValidationService validationService, DeviceImpl device) {
        this.amrSystem = amrSystem;
        this.validationService = validationService;
        this.device = device;
    }

    @Override
    public Device getDevice() {
        return device;
    }

    @Override
    public boolean isValidationActive(Date when) {
        Optional<Meter> found = device.findKoreMeter(amrSystem);
        return found.isPresent() && validationService.validationEnabled(found.get());
    }

    @Override
    public boolean isValidationActive(Channel channel, Date when) {
        if (!isValidationActive(when)) {
            return false;
        }
        Optional<com.elster.jupiter.metering.Channel> found = device.findKoreChannel(channel, when);
        return found.isPresent() && validationService.getMeterActivationValidations(found.get().getMeterActivation()).stream()
                .flatMap(m -> m.getChannelValidations().stream())
                .anyMatch(ChannelValidation::hasActiveRules);
    }

    @Override
    public Optional<Date> getLastChecked(Channel channel) {
        return device.findKoreChannels(channel).stream()
                .filter(k -> k.getReadingTypes().contains(channel.getReadingType()))
                .map(validationService::getLastChecked)
                .findFirst()
                .orElse(Optional.<Date>absent());
    }

    @Override
    public List<DataValidationStatus> getValidationStatus(Channel channel, Interval interval) {
        List<com.elster.jupiter.metering.Channel> koreChannels = ((DeviceImpl) channel.getDevice()).findKoreChannels(channel);
        return koreChannels.stream()
                .filter(k -> k.getMeterActivation().getInterval().overlaps(interval))
                .flatMap(k -> validationService.getValidationStatus(k, k.getMeterActivation().getInterval().intersection(interval)).stream())
                .collect(Collectors.toList());
    }
}
