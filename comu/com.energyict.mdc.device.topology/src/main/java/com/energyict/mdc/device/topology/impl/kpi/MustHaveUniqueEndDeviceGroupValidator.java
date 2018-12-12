/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl.kpi;

import com.energyict.mdc.device.topology.kpi.RegisteredDevicesKpi;
import com.energyict.mdc.device.topology.kpi.RegisteredDevicesKpiService;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

public class MustHaveUniqueEndDeviceGroupValidator implements ConstraintValidator<MustHaveUniqueEndDeviceGroup, RegisteredDevicesKpi> {

    private final RegisteredDevicesKpiService registeredDevicesKpiService;

    @Inject
    public MustHaveUniqueEndDeviceGroupValidator(RegisteredDevicesKpiService registeredDevicesKpiService) {
        this.registeredDevicesKpiService = registeredDevicesKpiService;
    }

    @Override
    public void initialize(MustHaveUniqueEndDeviceGroup mustHaveUniqueEndDeviceGroup) {
    }

    @Override
    public boolean isValid(RegisteredDevicesKpi registeredDevicesKpi, ConstraintValidatorContext constraintValidatorContext) {
        if (((RegisteredDevicesKpiImpl)registeredDevicesKpi).hasDeviceGroup()) {
            Optional<RegisteredDevicesKpi> kpiOptional = registeredDevicesKpiService.findRegisteredDevicesKpi(registeredDevicesKpi.getDeviceGroup());
            if (kpiOptional.isPresent() && kpiOptional.get().getId() != registeredDevicesKpi.getId()) {
                constraintValidatorContext.buildConstraintViolationWithTemplate(constraintValidatorContext.getDefaultConstraintMessageTemplate()).addPropertyNode("deviceGroup").addConstraintViolation().disableDefaultConstraintViolation();
                return false;
            }
        }
        return true;
    }
}