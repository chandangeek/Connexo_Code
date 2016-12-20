package com.energyict.mdc.device.data.impl.constraintvalidators;

import com.energyict.mdc.device.data.impl.DeviceMessageImpl;
import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Copyrights EnergyICT
 * Date: 10/29/14
 * Time: 11:59 AM
 */
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
                .map(deviceProtocolPluggableClass -> deviceProtocolPluggableClass.getDeviceProtocol().getSupportedMessages().stream()
                        .map(com.energyict.mdc.upl.messages.DeviceMessageSpec::getMessageId)
                        .map(DeviceMessageId::havingId)
                        .collect(Collectors.toList())).orElse(Collections.emptyList())
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
