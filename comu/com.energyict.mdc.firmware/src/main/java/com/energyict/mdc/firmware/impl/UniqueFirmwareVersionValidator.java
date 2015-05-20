package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.util.conditions.Where;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareVersion;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

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
        List<? extends FirmwareVersion> existing = ((FirmwareServiceImpl)firmwareService).getFirmwareVersionQuery().select(
                Where.where(FirmwareVersionImpl.Fields.DEVICETYPE.fieldName()).isEqualTo(in.getDeviceType())
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
