/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.constraintvalidators;

import com.energyict.mdc.device.data.impl.kpi.DataCollectionKpiImpl;
import com.energyict.mdc.device.data.kpi.DataCollectionKpi;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiService;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

public class MustHaveUniqueEndDeviceGroupValidator implements ConstraintValidator<MustHaveUniqueEndDeviceGroup, DataCollectionKpi> {

    private final DataCollectionKpiService dataCollectionKpiService;
    private String message;

    @Inject
    public MustHaveUniqueEndDeviceGroupValidator(DataCollectionKpiService dataCollectionKpiService) {
        this.dataCollectionKpiService = dataCollectionKpiService;
    }

    @Override
    public void initialize(MustHaveUniqueEndDeviceGroup mustHaveUniqueEndDeviceGroup) {
        message = mustHaveUniqueEndDeviceGroup.message();
    }

    @Override
    public boolean isValid(DataCollectionKpi dataCollectionKpi, ConstraintValidatorContext constraintValidatorContext) {
        if (((DataCollectionKpiImpl)dataCollectionKpi).hasDeviceGroup()) {
            Optional<DataCollectionKpi> kpiOptional = dataCollectionKpiService.findDataCollectionKpi(dataCollectionKpi.getDeviceGroup());
            if (kpiOptional.isPresent() && kpiOptional.get().getId() != dataCollectionKpi.getId()) {
                constraintValidatorContext.buildConstraintViolationWithTemplate(message).addPropertyNode("endDeviceGroup").addConstraintViolation().disableDefaultConstraintViolation();
                return false;
            }
        }
        return true;
    }
}