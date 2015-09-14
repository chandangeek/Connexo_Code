package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.config.RegisterSpec;

import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class NumericalRegisterSpecValidator implements ConstraintValidator<ValidNumericalRegisterSpec, NumericalRegisterSpec> {

    private final DataModel dataModel;

    @Inject
    public NumericalRegisterSpecValidator(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void initialize(ValidNumericalRegisterSpec constraintAnnotation) {
    }

    @Override
    public boolean isValid(NumericalRegisterSpec registerSpec, ConstraintValidatorContext context) {
        boolean valid=true;
        NumericalRegisterSpec freshRegisterSpec = (NumericalRegisterSpec) dataModel.mapper(RegisterSpec.class).getUnique("id", registerSpec.getId()).get();
        if (freshRegisterSpec.getDeviceConfiguration().isActive()) {
            if (freshRegisterSpec.getNumberOfDigits()>registerSpec.getNumberOfDigits()) {
                valid=false;
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("{"+ MessageSeeds.Keys.REGISTER_SPEC_NUMBER_OF_DIGITS_DECREASED+"}").
                        addPropertyNode(RegisterSpecFields.NUMBER_OF_DIGITS.fieldName()).
                        addConstraintViolation();
            }
            if (freshRegisterSpec.getNumberOfFractionDigits()>registerSpec.getNumberOfFractionDigits()) {
                valid=false;
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("{"+ MessageSeeds.Keys.REGISTER_SPEC_NUMBER_OF_FRACTION_DIGITS_DECREASED+"}").
                        addPropertyNode(RegisterSpecFields.NUMBER_OF_FRACTION_DIGITS.fieldName()).
                        addConstraintViolation();
            }
            if (freshRegisterSpec.getRegisterType().getId()!=registerSpec.getRegisterType().getId()) {
                valid=false;
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("{"+ MessageSeeds.Keys.REGISTER_SPEC_REGISTER_TYPE_ACTIVE_DEVICE_CONFIG +"}").
                        addPropertyNode(RegisterSpecFields.REGISTER_TYPE.fieldName()).
                        addConstraintViolation();
            }
        }
        return valid;
    }

}