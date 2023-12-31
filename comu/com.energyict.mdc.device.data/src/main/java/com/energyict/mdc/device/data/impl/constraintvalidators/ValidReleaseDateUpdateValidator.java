/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.constraintvalidators;

import com.energyict.mdc.device.data.impl.DeviceMessageImpl;
import com.energyict.mdc.device.data.impl.MessageSeeds;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ValidReleaseDateUpdateValidator implements ConstraintValidator<ValidReleaseDateUpdate, DeviceMessageImpl.ReleaseDateUpdater> {

    @Override
    public void initialize(ValidReleaseDateUpdate validReleaseDateUpdate) {
        // nothing to initialize
    }

    @Override
    public boolean isValid(DeviceMessageImpl.ReleaseDateUpdater releaseDateUpdater, ConstraintValidatorContext context) {
        if(releaseDateUpdater != null && !releaseDateUpdater.canUpdate()){
            context.disableDefaultConstraintViolation();
            context.
                    buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.DEVICE_MESSAGE_DONT_UPDATE_RELEASE_DATE_AFTER_SENT + "}").
                    addConstraintViolation();
            return false;
        }
        return true;
    }
}
