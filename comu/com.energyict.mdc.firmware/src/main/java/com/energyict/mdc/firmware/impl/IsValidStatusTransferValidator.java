/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.energyict.mdc.firmware.FirmwareStatus;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class IsValidStatusTransferValidator implements ConstraintValidator<IsValidStatusTransfer, FirmwareVersionImpl> {

    @Override
    public void initialize(IsValidStatusTransfer annotation) {
    }

    @Override
    public boolean isValid(FirmwareVersionImpl in, ConstraintValidatorContext context) {
        if (in.getOldFirmwareStatus() == null || (in.getOldFirmwareStatus() == in.getFirmwareStatus() && !FirmwareStatus.GHOST.equals(in.getOldFirmwareStatus()))) {
            return true;
        }

        switch (in.getFirmwareStatus()) {
            case DEPRECATED:
                return true;
            case FINAL:
                if (!FirmwareStatus.DEPRECATED.equals(in.getOldFirmwareStatus())) {
                    return true;
                }
                break;
            case TEST:
                if (FirmwareStatus.GHOST.equals(in.getOldFirmwareStatus())) {
                    return true;
                }
                break;
            case GHOST:
                if (in.getOldFirmwareStatus() == null) {
                    return true;
                }
                break;
        }

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate()).addPropertyNode("firmwareStatus").addConstraintViolation();
        return false;
    }
}
