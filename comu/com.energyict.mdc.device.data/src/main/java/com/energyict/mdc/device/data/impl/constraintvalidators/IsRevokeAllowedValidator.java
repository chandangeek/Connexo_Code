/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.constraintvalidators;

import com.energyict.mdc.device.data.impl.DeviceMessageImpl;
import com.energyict.mdc.device.data.impl.MessageSeeds;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class IsRevokeAllowedValidator implements ConstraintValidator<IsRevokeAllowed, DeviceMessageImpl.RevokeChecker> {

    @Override
    public void initialize(IsRevokeAllowed isRevokeAllowed) {
        // nothing to initialize
    }

    @Override
    public boolean isValid(DeviceMessageImpl.RevokeChecker revokeChecker, ConstraintValidatorContext context) {
        if (revokeChecker != null) {
            String messageSeedsKey = null;
            if (!revokeChecker.isRevokeStatusChangeAllowed()) {
                messageSeedsKey = MessageSeeds.Keys.DEVICE_MESSAGE_INVALID_REVOKE;
            } else if (revokeChecker.comServerHasPickedUpDeviceMessage()) {
                messageSeedsKey = MessageSeeds.Keys.DEVICE_MESSAGE_REVOKE_PICKED_UP_BY_COMSERVER;
            }

            if (messageSeedsKey != null) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("{" + messageSeedsKey + "}").addConstraintViolation();
                return false;
            }
        }
        return true;
    }
}
