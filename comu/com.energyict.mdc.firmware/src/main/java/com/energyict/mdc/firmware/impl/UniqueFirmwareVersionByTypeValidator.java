/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareVersion;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

public class UniqueFirmwareVersionByTypeValidator implements ConstraintValidator<UniqueFirmwareVersionByType, FirmwareVersion> {

    private final FirmwareService firmwareService;

    @Inject
    public UniqueFirmwareVersionByTypeValidator(FirmwareService firmwareService) {
        this.firmwareService = firmwareService;
    }

    @Override
    public void initialize(UniqueFirmwareVersionByType unique) {
    }

    @Override
    public boolean isValid(FirmwareVersion in, ConstraintValidatorContext context) {
        Optional<FirmwareVersion> firmwareVersionByVersionAndType = firmwareService.getFirmwareVersionByVersionAndType(in.getFirmwareVersion(), in.getFirmwareType(), in.getDeviceType());
        if (!firmwareVersionByVersionAndType.isPresent() || in.getId() == firmwareVersionByVersionAndType.get().getId()) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate()).addPropertyNode("firmwareVersion").addConstraintViolation();
        return false;
    }
}
