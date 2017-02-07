/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.config.RegisterSpec;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

public class ValidateUpdatableRegisterSpecFieldsValidator implements ConstraintValidator<ValidateUpdatableRegisterSpecFields, NumericalRegisterSpecImpl> {

    private final DeviceConfigurationService deviceConfigurationService;

    @Inject
    public ValidateUpdatableRegisterSpecFieldsValidator(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Override
    public void initialize(ValidateUpdatableRegisterSpecFields validateUpdatableRegisterSpecFields) {

    }

    @Override
    public boolean isValid(NumericalRegisterSpecImpl numericalRegisterSpec, ConstraintValidatorContext constraintValidatorContext) {
        if(numericalRegisterSpec.getDeviceConfiguration().isActive()){
            Optional<RegisterSpec> oldRegisterSpecOptional = deviceConfigurationService.findRegisterSpec(numericalRegisterSpec.getId());
            if(oldRegisterSpecOptional.isPresent()){
                NumericalRegisterSpec oldNumericalRegisterSpec = (NumericalRegisterSpec) oldRegisterSpecOptional.get();
                if (validateSameUsageOfMultiplier(numericalRegisterSpec, constraintValidatorContext, oldNumericalRegisterSpec)){
                    return false;
                }
                if (validateOverFlowAndFractionDigitsUpdate(numericalRegisterSpec, constraintValidatorContext, oldNumericalRegisterSpec)){
                    return false;
                }
            }
        }
        return true;
    }

    private boolean validateOverFlowAndFractionDigitsUpdate(NumericalRegisterSpecImpl numericalRegisterSpec, ConstraintValidatorContext constraintValidatorContext, NumericalRegisterSpec oldNumericalRegisterSpec) {
        if (oldNumericalRegisterSpec.getOverflowValue().isPresent() && numericalRegisterSpec.getOverflowValue().isPresent() &&
                oldNumericalRegisterSpec.getOverflowValue().get().compareTo(numericalRegisterSpec.getOverflowValue().get()) > 0) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate("{"+ MessageSeeds.Keys.REGISTER_SPEC_OVERFLOW_DECREASED +"}").
                    addPropertyNode(RegisterSpecFields.OVERFLOW_VALUE.fieldName()).
                    addConstraintViolation();
            return true;
        }
        if (oldNumericalRegisterSpec.getNumberOfFractionDigits()>numericalRegisterSpec.getNumberOfFractionDigits()) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate("{"+ MessageSeeds.Keys.REGISTER_SPEC_NUMBER_OF_FRACTION_DIGITS_DECREASED+"}").
                    addPropertyNode(RegisterSpecFields.NUMBER_OF_FRACTION_DIGITS.fieldName()).
                    addConstraintViolation();
            return true;
        }
        if (oldNumericalRegisterSpec.getRegisterType().getId()!=numericalRegisterSpec.getRegisterType().getId()) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate("{"+ MessageSeeds.Keys.REGISTER_SPEC_REGISTER_TYPE_ACTIVE_DEVICE_CONFIG +"}").
                    addPropertyNode(RegisterSpecFields.REGISTER_TYPE.fieldName()).
                    addConstraintViolation();
            return true;
        }
        return false;
    }

    private boolean validateSameUsageOfMultiplier(NumericalRegisterSpecImpl numericalRegisterSpec, ConstraintValidatorContext constraintValidatorContext, NumericalRegisterSpec oldNumericalRegisterSpec) {
        if(oldNumericalRegisterSpec.isUseMultiplier() == numericalRegisterSpec.isUseMultiplier()){
            if (validateSameCalculatedMultipliedReadingType(numericalRegisterSpec, constraintValidatorContext, oldNumericalRegisterSpec)){
                return true;
            }
        } else {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext
                    .buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.CANNOT_CHANGE_THE_USAGE_OF_THE_MULTIPLIER_OF_ACTIVE_CONFIG + "}")
                    .addPropertyNode(RegisterSpecFields.USEMULTIPLIER.fieldName())
                    .addConstraintViolation();
            return true;
        }
        return false;
    }

    private boolean validateSameCalculatedMultipliedReadingType(NumericalRegisterSpecImpl numericalRegisterSpec, ConstraintValidatorContext constraintValidatorContext, NumericalRegisterSpec oldNumericalRegisterSpec) {
        if(numericalRegisterSpec.isUseMultiplier()){
            if(oldNumericalRegisterSpec.getCalculatedReadingType().isPresent() && numericalRegisterSpec.getCalculatedReadingType().isPresent()){ // just making sure both are there
                if(!oldNumericalRegisterSpec.getCalculatedReadingType().get().equals(numericalRegisterSpec.getCalculatedReadingType().get())){
                    constraintValidatorContext.disableDefaultConstraintViolation();
                    constraintValidatorContext
                            .buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.CANNOT_CHANGE_MULTIPLIER_OF_ACTIVE_CONFIG + "}")
                            .addPropertyNode(RegisterSpecFields.CALCULATED_READINGTYPE.fieldName())
                            .addConstraintViolation();
                    return true;
                }
            }
        }
        return false;
    }
}
