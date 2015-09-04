package com.energyict.mdc.device.data.impl.constraintvalidators;

import com.energyict.mdc.device.data.impl.DeviceMessageImpl;
import com.energyict.mdc.device.data.impl.MessageSeeds;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Copyrights EnergyICT
 * Date: 11/5/14
 * Time: 3:26 PM
 */
public class IsRevokeAllowedValidator implements ConstraintValidator<IsRevokeAllowed, DeviceMessageImpl.RevokeChecker> {

    @Override
    public void initialize(IsRevokeAllowed isRevokeAllowed) {
        // nothing to initialize
    }

    @Override
    public boolean isValid(DeviceMessageImpl.RevokeChecker revokeChecker, ConstraintValidatorContext context) {
        if(revokeChecker != null && !revokeChecker.isRevokeAllowed()){
            context.disableDefaultConstraintViolation();
            context.
                    buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.DEVICE_MESSAGE_INVALID_REVOKE + "}").
                    addConstraintViolation();
            return false;
        }
        return true;
    }
}
