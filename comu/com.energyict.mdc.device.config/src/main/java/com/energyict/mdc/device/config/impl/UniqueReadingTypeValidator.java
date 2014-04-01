package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.RegisterMapping;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UniqueReadingTypeValidator implements ConstraintValidator<UniqueReadingType, RegisterMapping> {

    private final DeviceConfigurationService deviceConfigurationService;

    @Inject
    public UniqueReadingTypeValidator(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Override
    public void initialize(UniqueReadingType constraintAnnotation) {
    }

    @Override
    public boolean isValid(RegisterMapping value, ConstraintValidatorContext context) {
        RegisterMapping registerMapping = deviceConfigurationService.findRegisterMappingByReadingType(value.getReadingType());
        if (registerMapping!=null && registerMapping.getId()!=value.getId()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{"+MessageSeeds.Constants.REGISTER_MAPPING_DUPLICATE_READING_TYPE+"}").
                    addPropertyNode(RegisterMappingImpl.Fields.READING_TYPE.fieldName()).
                    addConstraintViolation();
            return false;
        }
        return true;
    }
}
