package com.energyict.mdc.device.data.impl.constraintvalidators;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.GatewayReferenceImpl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates that the gateway which is set on a GatewayReferenceImpl
 * is not the same as the origin Device.
 * <p/>
 * Copyrights EnergyICT
 * Date: 3/14/14
 * Time: 3:57 PM
 */
public class GatewayNotSameAsOriginValidator implements ConstraintValidator<CantBeOwnGateway, GatewayReferenceImpl> {

    private String message;

    @Override
    public void initialize(CantBeOwnGateway cantBeOwnGateway) {
        message = cantBeOwnGateway.message();
    }

    @Override
    public boolean isValid(GatewayReferenceImpl physicalGatewayReference, ConstraintValidatorContext constraintValidatorContext) {
        Device gateway = physicalGatewayReference.getGateway();
        if (gateway != null && gateway.getId() == physicalGatewayReference.getOrigin().getId()) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate(message).addPropertyNode("gateway").addConstraintViolation();
            return false;
        }
        return true;
    }

}