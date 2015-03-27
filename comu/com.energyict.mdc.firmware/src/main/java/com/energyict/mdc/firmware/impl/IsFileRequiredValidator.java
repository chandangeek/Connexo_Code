package com.energyict.mdc.firmware.impl;

import com.energyict.mdc.firmware.FirmwareStatus;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class IsFileRequiredValidator implements ConstraintValidator<IsFileRequired, FirmwareVersionImpl> {

    @Override
    public void initialize(IsFileRequired annotation) {
    }

    @Override
    public boolean isValid(FirmwareVersionImpl in, ConstraintValidatorContext context) {
        if(in.getOldFirmwareStatus() != null && in.getOldFirmwareStatus().equals(FirmwareStatus.GHOST)
                && !in.getFirmwareStatus().equals(FirmwareStatus.DEPRECATED) && in.getFirmwareFile() == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate()).addPropertyNode("firmwareFile").addConstraintViolation();
            return false;
        }
        return true;
    }
}
