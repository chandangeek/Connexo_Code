/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.constraintvalidators;

import com.energyict.mdc.device.data.impl.DeviceMessageImpl;
import com.energyict.mdc.device.data.impl.MessageSeeds;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Collections;

public class DeviceMessageIdValidator implements ConstraintValidator<ValidDeviceMessageId, DeviceMessageImpl> {

    @Override
    public void initialize(ValidDeviceMessageId validDeviceMessageId) {
        // intentionally not implemented
    }

    @Override
    public boolean isValid(DeviceMessageImpl deviceMessage, ConstraintValidatorContext constraintValidatorContext) {

        if (deviceMessage.getDevice()
                .getDeviceType()
                .getDeviceProtocolPluggableClass()
                .map(deviceProtocolPluggableClass -> deviceProtocolPluggableClass
                        .getDeviceProtocol().getSupportedMessages())
                .orElse(Collections.emptySet())
                .stream()
                .filter(deviceMessageId -> deviceMessageId.equals(deviceMessage.getDeviceMessageId()))
                .count() != 1) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.
                    buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.DEVICE_MESSAGE_ID_NOT_SUPPORTED + "}").
                    addPropertyNode(DeviceMessageImpl.Fields.DEVICEMESSAGEID.fieldName()).
                    addPropertyNode("device").
                    addConstraintViolation();
            return false;
        }
        if (!deviceMessage.getDevice()
                .getDeviceConfiguration()
                .getDeviceMessageEnablements()
                .stream()
                .filter(deviceMessageEnablement -> deviceMessageEnablement.getDeviceMessageId()
                        .equals(deviceMessage.getDeviceMessageId()))
                .findAny()
                .isPresent()) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.
                    buildConstraintViolationWithTemplate("{" + MessageSeeds.Keys.DEVICE_MESSAGE_NOT_ALLOWED_BY_CONFIG + "}").
                    addPropertyNode(DeviceMessageImpl.Fields.DEVICEMESSAGEID.fieldName()).
                    addPropertyNode("device").
                    addConstraintViolation();
            return false;
        }

        return true;
    }

}
