package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.GatewayType;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class GatewayTypeMustBeSpecifiedValidator implements ConstraintValidator<GatewayTypeMustBeSpecified, DeviceConfiguration> {
    private String message;

    @Override
    public void initialize(GatewayTypeMustBeSpecified constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(DeviceConfiguration deviceConfiguration, ConstraintValidatorContext context) {
        if (deviceConfiguration==null) {
            return true;
        }

        boolean valid=true;
        if (deviceConfiguration.canActAsGateway() && GatewayType.NONE.equals(deviceConfiguration.getGatewayType())){
            context.buildConstraintViolationWithTemplate(message)
                    .addPropertyNode(DeviceConfigurationImpl.Fields.GATEWAY_TYPE.fieldName())
                    .addConstraintViolation()
                    .disableDefaultConstraintViolation();
            valid = false;
        }
        return valid;
    }
}
