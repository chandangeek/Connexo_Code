package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.validation.DataValidationTask;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

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
        if (bothNotSet(deviceGroup, upGroup)) {
            //We don't know from what application this validation task is created, so will report both groups as required.
            //On front-end only one field will be highlighted depending on application: "Usage point group" for Insight and "Device group" for MS.
            //This should be changed when MDC/MDV demarcation will be implemented and validation task will "know" an origin app.
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{" + MessageSeeds.Constants.NAME_REQUIRED_KEY + "}")
                    .addPropertyNode("deviceGroup").addConstraintViolation();
            context.buildConstraintViolationWithTemplate("{" + MessageSeeds.Constants.NAME_REQUIRED_KEY + "}")
                    .addPropertyNode("usagePointGroup").addConstraintViolation();
            return false;
        }
        return true;
    }

    private boolean bothNotSet(Optional<EndDeviceGroup> deviceGroup, Optional<UsagePointGroup> upGroup) {
        return !deviceGroup.isPresent() && !upGroup.isPresent();
    }
}