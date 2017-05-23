/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.constraintvalidators;

import com.energyict.mdc.device.data.impl.DeviceMessageImpl;
import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Collections;
import java.util.stream.Collectors;

public class DeviceMessageIdValidator implements ConstraintValidator<ValidDeviceMessageId, DeviceMessageImpl> {

    @Override
    public void initialize(ValidDeviceMessageId validDeviceMessageId) {
        // intentionally not implemented
    }

    @Override
    public boolean isValid(DeviceMessageImpl deviceMessage, ConstraintValidatorContext constraintValidatorContext) {
        if (!isMessageSupportedByProtocol(deviceMessage)){
            addConstraintViolation(constraintValidatorContext, MessageSeeds.Keys.DEVICE_MESSAGE_ID_NOT_SUPPORTED) ;
            return false;
        }
        if (!isMessageAllowedByConfig(deviceMessage)){
            addConstraintViolation(constraintValidatorContext, MessageSeeds.Keys.DEVICE_MESSAGE_NOT_ALLOWED_BY_CONFIG) ;
            return false;
        }
        return true;
    }

    private boolean isMessageSupportedByProtocol(DeviceMessageImpl deviceMessage){
         return deviceMessage.getDevice()
                         .getDeviceType()
                         .getDeviceProtocolPluggableClass()
                         .map(deviceProtocolPluggableClass -> deviceProtocolPluggableClass.getDeviceProtocol().getSupportedMessages().stream()
                                 .map(com.energyict.mdc.upl.messages.DeviceMessageSpec::getId)
                                 .map(DeviceMessageId::from)
                                 .collect(Collectors.toList())).orElse(Collections.emptyList())
                         .stream()
                         .filter(deviceMessageId -> deviceMessageId.equals(deviceMessage.getDeviceMessageId()))
                         .count() == 1;
    }

    private boolean isMessageAllowedByConfig(DeviceMessageImpl deviceMessage){
        return deviceMessage.getDevice()
                        .getDeviceConfiguration()
                        .getDeviceMessageEnablements()
                        .stream()
                        .filter(deviceMessageEnablement -> deviceMessageEnablement.getDeviceMessageId()
                                .equals(deviceMessage.getDeviceMessageId()))
                        .findAny()
                        .isPresent();
    }

    private void addConstraintViolation( ConstraintValidatorContext constraintValidatorContext, String messageSeedKey){
        constraintValidatorContext.disableDefaultConstraintViolation();
        constraintValidatorContext.
                buildConstraintViolationWithTemplate("{" + messageSeedKey + "}").
                addPropertyNode(DeviceMessageImpl.Fields.DEVICEMESSAGEID.fieldName()).
                addPropertyNode("device").
                addConstraintViolation();
    }


}
