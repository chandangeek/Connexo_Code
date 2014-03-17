package com.energyict.mdc.device.data.impl.constraintvalidators;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.PhysicalGatewayReferenceImpl;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates that the gateway which is set to the PhysicalGatewayReference
 * is not the same as the origin Device
 * <p/>
 * Copyrights EnergyICT
 * Date: 3/14/14
 * Time: 3:57 PM
 */
public class PhysicalGatewayNotSameAsOriginValidator implements ConstraintValidator<CantBeOwnGateway, PhysicalGatewayReferenceImpl> {

    private String message;

    @Inject
    public PhysicalGatewayNotSameAsOriginValidator() {
    }

    @Override
    public void initialize(CantBeOwnGateway cantBeOwnGateway) {
        message = cantBeOwnGateway.message();
    }

    @Override
    public boolean isValid(PhysicalGatewayReferenceImpl physicalGatewayReference, ConstraintValidatorContext constraintValidatorContext) {
        Device physicalGateway = physicalGatewayReference.getPhysicalGateway();

        if (physicalGateway != null && physicalGateway.getId() == physicalGatewayReference.getOrigin().getId()) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate(message).addPropertyNode("gateway").addConstraintViolation();
            return false;
        }
        return true;
    }
}
