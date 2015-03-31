package com.energyict.mdc.firmware.impl;

import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareStatus;
import com.energyict.mdc.firmware.FirmwareVersion;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

public class IsFileRequiredValidator implements ConstraintValidator<IsFileRequired, FirmwareVersionImpl> {
    private final FirmwareService firmwareService;

    @Inject
    public IsFileRequiredValidator(FirmwareService firmwareService) {
        this.firmwareService = firmwareService;
    }

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

        if(!FirmwareStatus.GHOST.equals(in.getFirmwareStatus()) && in.getFirmwareFile() == null) {
            if (in.getId() != 0) {
                Optional<FirmwareVersion> versionOptional = firmwareService.getFirmwareVersionById(in.getId());
                if (versionOptional.isPresent() && versionOptional.get().getFirmwareFile() == null) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate()).addPropertyNode("firmwareFile").addConstraintViolation();
                    return false;
                }
            } else {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate()).addPropertyNode("firmwareFile").addConstraintViolation();
                return false;
            }
        }
        return true;
    }
}
