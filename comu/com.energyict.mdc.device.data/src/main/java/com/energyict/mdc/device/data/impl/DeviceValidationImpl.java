package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceValidation;
import com.google.common.base.Optional;

import java.util.Date;

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
        if (found.isPresent()) {
            return !validationService.getMeterActivationValidations(found.get().getMeterActivation()).isEmpty();
        }
        return false;
    }
}
