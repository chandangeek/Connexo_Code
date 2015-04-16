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
    public boolean isValid(FirmwareVersionImpl firmwareVersion, ConstraintValidatorContext context) {
        if(FirmwareStatus.GHOST.equals(firmwareVersion.getOldFirmwareStatus())
                && !FirmwareStatus.DEPRECATED.equals(firmwareVersion.getFirmwareStatus()) && firmwareVersion.getFirmwareFile() == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate()).addPropertyNode("firmwareFile").addConstraintViolation();
            return false;
        }

        if(!FirmwareStatus.GHOST.equals(firmwareVersion.getFirmwareStatus()) && firmwareVersion.getFirmwareFile() == null) {
            if (firmwareVersion.getId() != 0) {
                Optional<FirmwareVersion> versionOptional = firmwareService.getFirmwareVersionById(firmwareVersion.getId());
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
