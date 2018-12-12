/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.energyict.mdc.firmware.FirmwareStatus;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class IsStatusRequiredValidator implements ConstraintValidator<IsStatusRequired, FirmwareVersionImpl> {
    @Override
    public void initialize(IsStatusRequired annotation) {
    }

    @Override
    public boolean isValid(FirmwareVersionImpl firmwareVersion, ConstraintValidatorContext context) {
        if (FirmwareStatus.GHOST.equals(firmwareVersion.getOldFirmwareStatus()) && firmwareVersion.getFirmwareStatus().equals(FirmwareStatus.GHOST)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate()).addPropertyNode("firmwareStatus").addConstraintViolation();
            return false;
        }
        return true;
    }
}
