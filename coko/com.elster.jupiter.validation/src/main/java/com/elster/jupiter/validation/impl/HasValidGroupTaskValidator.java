package com.elster.jupiter.validation.impl;

import java.util.Optional;

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
        Optional<EndDeviceGroup> deviceGroup = validationTask.getEndDeviceGroup();
        Optional<UsagePointGroup> upGroup = validationTask.getUsagePointGroup();
        
        return bothNotNull(deviceGroup, upGroup) && bothNotSet(deviceGroup, upGroup);
    }

    private boolean bothNotSet(Optional<EndDeviceGroup> deviceGroup, Optional<UsagePointGroup> upGroup) {
        return !(deviceGroup.isPresent() && upGroup.isPresent());
    }

    private boolean bothNotNull(Optional<EndDeviceGroup> deviceGroup, Optional<UsagePointGroup> upGroup) {
        return (deviceGroup.isPresent() || upGroup.isPresent());
    }

}