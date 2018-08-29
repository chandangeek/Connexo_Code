package com.energyict.protocolimplv2.messages.validators;

import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageAttribute;
import com.energyict.protocol.exception.DeviceConfigurationException;
import com.energyict.protocol.exceptions.ProtocolRuntimeException;

public class BeaconMessageValidator {

    public static void validateAlarmFilter(DeviceMessage deviceMessage, String attributeName) {
        String deviceMessageAttributeValue = getDeviceMessageAttributeValue(deviceMessage, attributeName);

        if (deviceMessageAttributeValue != null) {
            if (!deviceMessageAttributeValue.matches("^[0-9]+$")) {
                String failReason = "You must enter a valid value for the alarm filter, numbers only.";
                throw unsupportedPropertyValueWithReason(attributeName, deviceMessageAttributeValue, failReason);
            }
        } else {
            String failReason = "You must enter a valid value for the alarm filter, null received.";
            throw unsupportedPropertyValueWithReason(attributeName, "null", failReason);
        }
    }

    public static String getDeviceMessageAttributeValue(DeviceMessage deviceMessage, String attributeName) {
        DeviceMessageAttribute deviceMessageAttribute = getDeviceMessageAttribute(deviceMessage, attributeName);
        if (deviceMessageAttribute == null){
            return null;
        }

        return (String) deviceMessageAttribute.getValue();
    }

    public static DeviceMessageAttribute getDeviceMessageAttribute(DeviceMessage deviceMessage, String attributeName) {
        for (DeviceMessageAttribute deviceMessageAttribute : deviceMessage.getAttributes()) {
            if (deviceMessageAttribute.getName().equals(attributeName)) {
                return deviceMessageAttribute;
            }
        }
        return null;
    }

    private static ProtocolRuntimeException unsupportedPropertyValueWithReason(String propertyName, String propertyValue, String message) {
        return DeviceConfigurationException.unsupportedPropertyValueWithReason(propertyName, propertyValue, message);
    }

}
