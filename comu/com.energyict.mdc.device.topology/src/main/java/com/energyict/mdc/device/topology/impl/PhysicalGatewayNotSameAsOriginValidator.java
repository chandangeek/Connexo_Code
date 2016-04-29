package com.energyict.mdc.device.topology.impl;

import com.energyict.mdc.device.data.Device;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates that the gateway which is set on a PhysicalGatewayReferenceImpl
 * is not the same as the origin Device.
 * <p/>
 * Copyrights EnergyICT
 * Date: 3/14/14
 * Time: 3:57 PM
 */
public class PhysicalGatewayNotSameAsOriginValidator implements ConstraintValidator<PhysicalGatewayNotSameAsOrigin, PhysicalGatewayReference> {

    private String message;

    @Override
    public void initialize(PhysicalGatewayNotSameAsOrigin physicalGatewayNotSameAsOrigin) {
        message = physicalGatewayNotSameAsOrigin.message();
    }

    @Override
    public boolean isValid(PhysicalGatewayReference physicalGatewayReference, ConstraintValidatorContext constraintValidatorContext) {
        Device gateway = physicalGatewayReference.getGateway();
        if (gateway != null && gateway.getId() == physicalGatewayReference.getOrigin().getId()) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate(message).addPropertyNode("gateway").addConstraintViolation();
            return false;
        }
        return true;
    }

}