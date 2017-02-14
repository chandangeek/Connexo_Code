/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl.kpi;

import com.elster.jupiter.validation.kpi.DataValidationKpi;
import com.elster.jupiter.validation.kpi.DataValidationKpiService;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

public class MustHaveUniqueEndDeviceGroupValidator implements ConstraintValidator<MustHaveUniqueEndDeviceGroup, DataValidationKpi> {

    private final DataValidationKpiService dataValidationKpiService;
    private String message;

    @Inject
    public MustHaveUniqueEndDeviceGroupValidator(DataValidationKpiService dataValidationKpiService) {
        this.dataValidationKpiService = dataValidationKpiService;
    }

    @Override
    public void initialize(MustHaveUniqueEndDeviceGroup mustHaveUniqueEndDeviceGroup) {
        message = mustHaveUniqueEndDeviceGroup.message();
    }

    @Override
    public boolean isValid(DataValidationKpi dataValidationKpi, ConstraintValidatorContext constraintValidatorContext) {
        if (((DataValidationKpiImpl)dataValidationKpi).hasDeviceGroup()) {
            Optional<DataValidationKpi> kpiOptional = dataValidationKpiService.findDataValidationKpi(dataValidationKpi.getDeviceGroup());
            if (kpiOptional.isPresent() && kpiOptional.get().getId() != dataValidationKpi.getId()) {
                constraintValidatorContext.buildConstraintViolationWithTemplate(message).addPropertyNode("endDeviceGroup").addConstraintViolation().disableDefaultConstraintViolation();
                return false;
            }
        }
        return true;
    }

}
