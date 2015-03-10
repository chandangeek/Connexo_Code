package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.domain.util.Unique;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.conditions.Where;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareVersion;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UniqueFirmwareVersionValidator  implements ConstraintValidator<UniqueFirmwareVersion, FirmwareVersion> {
    @Inject
    private DataModel dataModel;
    private String[] fields;

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
        List<? extends FirmwareVersion> existing = firmwareService.getFirmwareVersionQuery().select(Where.where("deviceType").isEqualTo(in.getDeviceType()));
        if (existing.isEmpty()) {
            return true;
        } else if (existing.size() == 1 && in.getId() == existing.get(0).getId()) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate()).addPropertyNode("deviceType").addConstraintViolation();
        return false;
    }
}
