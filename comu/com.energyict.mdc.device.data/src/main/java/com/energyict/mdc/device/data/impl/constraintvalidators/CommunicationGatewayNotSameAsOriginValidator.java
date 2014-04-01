package com.energyict.mdc.device.data.impl.constraintvalidators;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.CommunicationGatewayReferenceImpl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates that the gateway which is set to the CommunicationGatewayReference
 * is not the same as the origin Device
 * <p/>
 * Copyrights EnergyICT
 * Date: 3/14/14
 * Time: 4:39 PM
 */
public class CommunicationGatewayNotSameAsOriginValidator implements ConstraintValidator<CantBeOwnGateway, CommunicationGatewayReferenceImpl> {

    private String message;

    @Override
    public void initialize(CantBeOwnGateway cantBeOwnGateway) {
        message = cantBeOwnGateway.message();
    }

    @Override
    public boolean isValid(CommunicationGatewayReferenceImpl communicationGatewayReference, ConstraintValidatorContext constraintValidatorContext) {
        Device communicationGateway = communicationGatewayReference.getCommunicationGateway();

        if (communicationGateway != null && communicationGateway.getId() == communicationGatewayReference.getOrigin().getId()) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate(message).addPropertyNode("gateway").addConstraintViolation();
            return false;
        }
        return true;    }
}
