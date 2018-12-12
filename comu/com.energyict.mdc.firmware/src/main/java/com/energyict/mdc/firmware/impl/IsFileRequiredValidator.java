/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.energyict.mdc.firmware.FirmwareStatus;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class IsFileRequiredValidator implements ConstraintValidator<IsFileRequired, FirmwareVersionImpl> {

    public IsFileRequiredValidator() {}

    @Override
    public void initialize(IsFileRequired annotation) {
    }

    @Override
    public boolean isValid(FirmwareVersionImpl firmwareVersion, ConstraintValidatorContext context) {
        /*
         - A firmware version with 'Final' or 'Test' status MUST have a firmware file
         - A deprecated firmware version never has a firmware file
         - A ghost firmware version never has a firmware file (when we edit the ghost version we MUST specify the 'Final' (or 'Test') status)
         */
        if (!FirmwareStatus.GHOST.equals(firmwareVersion.getFirmwareStatus())
                && !FirmwareStatus.DEPRECATED.equals(firmwareVersion.getFirmwareStatus())) {
            if (!firmwareVersion.hasFirmwareFile() && firmwareVersion.getId() == 0) {   // New firmware version created without firmware file
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                        .addPropertyNode("firmwareFile")
                        .addConstraintViolation();
                return false;
            } else if (firmwareVersion.isEmptyFile()) {    // Firmware version created/updated with empty firmware file
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.FILE_IS_EMPTY + "}")
                        .addPropertyNode("firmwareFile")
                        .addConstraintViolation();
                return false;
            }
        }

        return true;
    }
}
