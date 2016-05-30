package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.validation.DataValidationTask;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

public class HasValidGroupTaskValidator implements ConstraintValidator<HasValidGroup, DataValidationTask> {

    public static final String MULTISENSE_KEY = "MDC";
    public static final String INSIGHT_KEY = "INS";

    public HasValidGroupTaskValidator() {
    }

    @Override
    public void initialize(HasValidGroup constraintAnnotation) {
    }

    @Override
    public boolean isValid(DataValidationTask validationTask, ConstraintValidatorContext context) {
        Optional<EndDeviceGroup> deviceGroup = validationTask.getEndDeviceGroup();
        Optional<MetrologyContract> metrologyContract = validationTask.getMetrologyContract();
        String applicationName = validationTask.getApplication();


        context.disableDefaultConstraintViolation();
        switch (applicationName) {
            case MULTISENSE_KEY: {
                if (!deviceGroup.isPresent()) {
                    context.buildConstraintViolationWithTemplate("{" + MessageSeeds.Constants.NAME_REQUIRED_KEY + "}")
                            .addPropertyNode("deviceGroup").addConstraintViolation();
                    return false;
                } break;
            }
            case INSIGHT_KEY: {
                if (!metrologyContract.isPresent()) {
                    context.buildConstraintViolationWithTemplate("{" + MessageSeeds.Constants.NAME_REQUIRED_KEY + "}")
                            .addPropertyNode("metrologyContract").addConstraintViolation();
                    return false;
                } break;
            }
        }
        return true;
    }
}