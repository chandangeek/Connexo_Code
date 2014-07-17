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
            context.buildConstraintViolationWithTemplate("{"+ MessageSeeds.Keys.REGISTER_SPEC_NUMBER_OF_DIGITS_DECREASED+"}").
                    addPropertyNode(RegisterSpecImpl.Fields.NUMBER_OF_DIGITS.fieldName()).
                    addConstraintViolation();
        }
        if (freshRegisterSpec.getNumberOfFractionDigits()>registerSpec.getNumberOfFractionDigits()) {
            valid=false;
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{"+ MessageSeeds.Keys.REGISTER_SPEC_NUMBER_OF_FRACTION_DIGITS_DECREASED+"}").
                    addPropertyNode(RegisterSpecImpl.Fields.NUMBER_OF_FRACTION_DIGITS.fieldName()).
                    addConstraintViolation();
        }
        if (freshRegisterSpec.getDeviceConfiguration().isActive()) {
            if (freshRegisterSpec.getRegisterType().getId()!=registerSpec.getRegisterType().getId()) {
                valid=false;
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("{"+ MessageSeeds.Keys.REGISTER_SPEC_REGISTER_TYPE_ACTIVE_DEVICE_CONFIG +"}").
                        addPropertyNode(RegisterSpecImpl.Fields.REGISTER_TYPE.fieldName()).
                        addConstraintViolation();
            }
            if (!freshRegisterSpec.getMultiplier().equals(registerSpec.getMultiplier())) {
                valid=false;
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("{"+ MessageSeeds.Keys.REGISTER_SPEC_MULTIPLIER_ACTIVE_DEVICE_CONFIG+"}").
                        addPropertyNode(RegisterSpecImpl.Fields.MULTIPLIER.fieldName()).
                        addConstraintViolation();
            }
        }
        return valid;
    }
}
