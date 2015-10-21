package com.elster.jupiter.validation.impl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.validation.DataValidationTask;

public class HasValidGroupTaskValidator implements ConstraintValidator<HasValidGroup, DataValidationTask> {

    public HasValidGroupTaskValidator(){
    }

    @Override
    public void initialize(HasValidGroup constraintAnnotation) {
    }

    @Override
    public boolean isValid(DataValidationTask validationTask, ConstraintValidatorContext context) {
        EndDeviceGroup deviceGroup = validationTask.getEndDeviceGroup();
        UsagePointGroup upGroup = validationTask.getUsagePointGroup();
        
        return bothNotNull(deviceGroup, upGroup) && bothNotSet(deviceGroup, upGroup);
    }

    private boolean bothNotSet(EndDeviceGroup deviceGroup, UsagePointGroup upGroup) {
        return !(deviceGroup != null && upGroup != null);
    }

    private boolean bothNotNull(EndDeviceGroup deviceGroup, UsagePointGroup upGroup) {
        return !(deviceGroup == null && upGroup == null);
    }

}