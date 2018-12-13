/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dataquality.impl;

import com.elster.jupiter.dataquality.DataQualityKpiService;
import com.elster.jupiter.dataquality.DeviceDataQualityKpi;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

import static com.elster.jupiter.util.streams.Predicates.not;

public class UniqueEndDeviceGroupValidator implements ConstraintValidator<UniqueEndDeviceGroup, DeviceDataQualityKpiImpl> {

    private final DataQualityKpiService dataQualityKpiService;

    private String message;

    @Inject
    public UniqueEndDeviceGroupValidator(DataQualityKpiService dataQualityKpiService) {
        this.dataQualityKpiService = dataQualityKpiService;
    }

    @Override
    public void initialize(UniqueEndDeviceGroup uniqueEndDeviceGroup) {
        message = uniqueEndDeviceGroup.message();
    }

    @Override
    public boolean isValid(DeviceDataQualityKpiImpl dataQualityKpi, ConstraintValidatorContext constraintValidatorContext) {
        if (dataQualityKpi.getDeviceGroup() == null) {
            return true; // @IsPresent constraint violation should work
        }
        List<DeviceDataQualityKpi> existingKpis = dataQualityKpiService.deviceDataQualityKpiFinder().forGroup(dataQualityKpi.getDeviceGroup()).find();
        if (existingKpis.stream().anyMatch(not(dataQualityKpi::equals))) {
            constraintValidatorContext.buildConstraintViolationWithTemplate(message)
                    .addPropertyNode(DeviceDataQualityKpiImpl.Fields.ENDDEVICE_GROUP.fieldName())
                    .addConstraintViolation()
                    .disableDefaultConstraintViolation();
            return false;
        }
        return true;
    }
}
