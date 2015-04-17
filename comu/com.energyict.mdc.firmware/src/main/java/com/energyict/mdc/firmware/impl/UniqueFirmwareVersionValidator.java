package com.energyict.mdc.firmware.impl;

import java.util.List;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.elster.jupiter.util.conditions.Where;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareVersion;

public class UniqueFirmwareVersionValidator  implements ConstraintValidator<UniqueFirmwareVersion, FirmwareVersion> {

    private final FirmwareService firmwareService;

    @Inject
    public UniqueFirmwareVersionValidator(FirmwareService firmwareService) {
        this.firmwareService = firmwareService;
    }

    @Override
    public void initialize(UniqueFirmwareVersion unique) {
    }

    @Override
    public boolean isValid(FirmwareVersion in, ConstraintValidatorContext context) {
        List<? extends FirmwareVersion> existing = firmwareService.getFirmwareVersionQuery().select(Where.where("deviceType").isEqualTo(in.getDeviceType())
                .and(Where.where(FirmwareVersionImpl.Fields.FIRMWAREVERSION.fieldName()).isEqualTo(in.getFirmwareVersion())));
        if (existing.isEmpty()) {
            return true;
        } else if (existing.size() == 1 && in.getId() == existing.get(0).getId()) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate()).addPropertyNode("firmwareVersion").addConstraintViolation();
        return false;
    }
}
