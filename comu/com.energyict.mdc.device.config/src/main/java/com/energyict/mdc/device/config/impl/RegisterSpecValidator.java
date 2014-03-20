package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class RegisterSpecValidator implements ConstraintValidator<ValidRegisterSpec, RegisterSpec> {

    private final DataModel dataModel;

    @Inject
    public RegisterSpecValidator(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void initialize(ValidRegisterSpec constraintAnnotation) {

    }

    @Override
    public boolean isValid(RegisterSpec registerSpec, ConstraintValidatorContext context) {
        boolean valid=true;
        RegisterSpec freshRegisterSpec = dataModel.mapper(RegisterSpec.class).getUnique("id", registerSpec.getId()).get();
        if (freshRegisterSpec.getNumberOfDigits()>registerSpec.getNumberOfDigits()) {
            valid=false;
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{"+MessageSeeds.REGISTER_SPEC_NUMBER_OF_DIGITS_DECREASED.getKey()+"}").addPropertyNode(RegisterSpecImpl.NUMBER_OF_DIGITS).addConstraintViolation();
        }
        if (freshRegisterSpec.getNumberOfFractionDigits()>registerSpec.getNumberOfFractionDigits()) {
            valid=false;
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{"+MessageSeeds.REGISTER_SPEC_NUMBER_OF_FRACTION_DIGITS_DECREASED.getKey()+"}").addPropertyNode(RegisterSpecImpl.NUMBER_OF_FRACTION_DIGITS).addConstraintViolation();
        }
        if (freshRegisterSpec.getDeviceConfiguration().isActive()) {
            if (freshRegisterSpec.getRegisterMapping().getId()!=registerSpec.getRegisterMapping().getId()) {
                valid=false;
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("{"+MessageSeeds.REGISTER_SPEC_REGISTER_MAPPING_CAN_NOT_CHANGE_FOR_ACTIVE_CONFIG.getKey()+"}").addPropertyNode(RegisterSpecImpl.REGISTER_MAPPING).addConstraintViolation();
            }
            if (!freshRegisterSpec.getMultiplier().equals(registerSpec.getMultiplier())) {
                valid=false;
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("{"+MessageSeeds.REGISTER_SPEC_MULTIPLIER_CAN_NOT_CHANGE_FOR_ACTIVE_CONFIG.getKey()+"}").addPropertyNode(RegisterSpecImpl.MULTIPLIER).addConstraintViolation();
            }
        }
        return valid;
    }
}
