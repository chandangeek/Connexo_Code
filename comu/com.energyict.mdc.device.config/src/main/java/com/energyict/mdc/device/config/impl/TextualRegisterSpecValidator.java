package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.config.TextualRegisterSpec;

import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class TextualRegisterSpecValidator implements ConstraintValidator<ValidTextualRegisterSpec, TextualRegisterSpec> {

    private final DataModel dataModel;

    @Inject
    public TextualRegisterSpecValidator(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void initialize(ValidTextualRegisterSpec constraintAnnotation) {
    }

    @Override
    public boolean isValid(TextualRegisterSpec registerSpec, ConstraintValidatorContext context) {
        TextualRegisterSpec oldVersion = this.getOldVersion(registerSpec);
        if (this.registerTypeChangedInActiveConfiguration(registerSpec,  oldVersion)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.REGISTER_SPEC_REGISTER_TYPE_ACTIVE_DEVICE_CONFIG + "}").
                    addPropertyNode(RegisterSpecFields.REGISTER_TYPE.fieldName()).
                    addConstraintViolation();
            return false;
        }
        else {
            return true;
        }
    }

    private TextualRegisterSpec getOldVersion(TextualRegisterSpec registerSpec) {
        return (TextualRegisterSpec) this.dataModel.mapper(RegisterSpec.class).getUnique("id", registerSpec.getId()).get();
    }

    private boolean registerTypeChangedInActiveConfiguration (TextualRegisterSpec validationTarget, TextualRegisterSpec oldVersion) {
        if (oldVersion.getDeviceConfiguration().isActive()) {
            return oldVersion.getRegisterType().getId() != validationTarget.getRegisterType().getId();
        }
        else {
            return false;
        }
    }

}