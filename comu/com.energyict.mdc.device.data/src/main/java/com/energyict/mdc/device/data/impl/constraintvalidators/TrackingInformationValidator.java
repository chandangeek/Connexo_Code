/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.constraintvalidators;

import com.energyict.mdc.device.data.impl.DeviceMessageImpl;
import com.energyict.mdc.protocol.api.TrackingCategory;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Created by bvn on 3/14/16.
 */
public class TrackingInformationValidator implements ConstraintValidator<ValidTrackingInformation, DeviceMessageImpl> {
    private String message;

    @Override
    public void initialize(ValidTrackingInformation validTrackingInformation) {
        message = validTrackingInformation.message();
    }

    @Override
    public boolean isValid(DeviceMessageImpl deviceMessage, ConstraintValidatorContext constraintValidatorContext) {
        if (deviceMessage.getTrackingCategory() != null
                && deviceMessage.getTrackingCategory().equals(TrackingCategory.serviceCall)
                && deviceMessage.getTrackingId() == null) {
            constraintValidatorContext.buildConstraintViolationWithTemplate(message)
                    .addPropertyNode(DeviceMessageImpl.Fields.TRACKINGID.fieldName())
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
