package com.energyict.protocolimplv2.messages.validators;

import com.energyict.cbo.Password;
import com.energyict.mdc.messages.DeviceMessage;
import com.energyict.mdc.messages.DeviceMessageAttribute;
import com.energyict.mdc.protocol.security.SecurityProperty;
import com.energyict.mdc.protocol.security.SecurityPropertySet;
import com.energyict.mdw.core.Device;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.protocol.exceptions.DeviceConfigurationException;
import com.energyict.protocol.exceptions.ProtocolRuntimeException;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.enums.ClientSecuritySetup;
import com.energyict.protocolimplv2.security.SecurityPropertySpecName;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cisac on 11/25/2016.
 */
public class KeyMessageChangeValidator {

    /**
     * Does a pre-verification of the new key
     */
    public void validateNewKeyValue(int deviceId, DeviceMessage deviceMessage, SecurityPropertySpecName securityPropertySpecName){
        int clientId = getClientId(deviceMessage);
        validateNewKeyValueForClient(deviceId, deviceMessage, securityPropertySpecName, clientId);
    }

    /**
     * Does a pre-verification of the new key
     */
    public void validateNewKeyValueForFreeTextClient(int deviceId, DeviceMessage deviceMessage, SecurityPropertySpecName securityPropertySpecName){
        int clientId = getFreeTextClientId(deviceMessage);
        validateNewKeyValueForClient(deviceId, deviceMessage, securityPropertySpecName, clientId);
    }

    /**
     * Does a pre-verification of the new key
     */
    public void validateNewKeyValueForClient(int deviceId, DeviceMessage deviceMessage, SecurityPropertySpecName securityPropertySpecName, int clientId){
        final Device device = getMeteringWarehouse().getDeviceFactory().find(deviceId);
        String newKey = ((Password) getDeviceMessageAttribute(deviceMessage, getAttributeNameForSecurityProperty(securityPropertySpecName)).getValue()).getValue();

        String configuredKeyForClient = getSecurityKey(device, securityPropertySpecName.toString(), clientId, deviceMessage, newKey);

        if(configuredKeyForClient == null){
            String message = "There is no "+securityPropertySpecName+" defined for client "+configuredKeyForClient;
            deviceMessage.fail(message);
            throw missingProperty(securityPropertySpecName.toString());
        } else if(configuredKeyForClient.equalsIgnoreCase(newKey)){
            String failReason = "This key is the same with the already in use key for client " + getClientId(deviceMessage) + ". Please use another key";
            deviceMessage.fail(failReason);
            throw unsupportedPropertyValueWithReason(getAttributeNameForSecurityProperty(securityPropertySpecName), newKey, failReason);
        }
    }

    public void validateSecurityKeyLength(SecurityPropertySet securityPropertySet, String propertyName, String key, DeviceMessage deviceMessage) {
        int securitySuite = securityPropertySet.getSecuritySuiteId();
        int suite2Length = 64;
        int defaultLength = 32;

        if(securitySuite == 2 && key.length() != suite2Length){
            String failedReason = "The length of the security key is incorrect. Expected " + suite2Length + " but was " + key.length();
            deviceMessage.fail(failedReason);
            unsupportedPropertyValueLengthWithReason(propertyName, String.valueOf(suite2Length), failedReason);
        } else if(key.length() != defaultLength) {
            //for other suites use defaultLength
            String failedReason = "The length of the security key is incorrect. Expected " + defaultLength + " but was " + key.length();
            deviceMessage.fail(failedReason);
            unsupportedPropertyValueLengthWithReason(propertyName, String.valueOf(defaultLength), failedReason);
        }
    }

    private String getAttributeNameForSecurityProperty(SecurityPropertySpecName securityPropertySpecName){
        switch (securityPropertySpecName) {
            case AUTHENTICATION_KEY:
                return DeviceMessageConstants.newAuthenticationKeyAttributeName;
            case ENCRYPTION_KEY:
                return DeviceMessageConstants.newEncryptionKeyAttributeName;
            case MASTER_KEY:
                return DeviceMessageConstants.newMasterKeyAttributeName;
        }
        return "";
    }

    protected int getClientId(DeviceMessage deviceMessage){
        String client = getDeviceMessageAttributeValue(deviceMessage, DeviceMessageConstants.client);
        if (client!=null && !client.isEmpty()) {
            try{
                return ClientSecuritySetup.valueOf(client).getID();
            } catch (Exception ex){
                // ignore
            }
        }
        return 1;
    }

    protected String getDeviceMessageAttributeValue(DeviceMessage deviceMessage, String attributeName){
        DeviceMessageAttribute deviceMessageAttribute = getDeviceMessageAttribute(deviceMessage, attributeName);
        if (deviceMessageAttribute == null){
            return null;
        }

        return (String) deviceMessageAttribute.getValue();
    }

    protected int getFreeTextClientId(DeviceMessage deviceMessage){
        String client = getDeviceMessageAttributeValue(deviceMessage, DeviceMessageConstants.clientMacAddress);
        if (client!=null) {
            if (!client.isEmpty()) {
                try{
                    return Integer.parseInt(client);
                } catch (Exception ex){
                    // swallow
                }
            }
        }
        return 1;
    }

    public DeviceMessageAttribute getDeviceMessageAttribute(DeviceMessage deviceMessage, String attributeName) {
        for (DeviceMessageAttribute deviceMessageAttribute : deviceMessage.getAttributes()) {
            if (deviceMessageAttribute.getName().equals(attributeName)) {
                return deviceMessageAttribute;
            }
        }
        return null;
    }

    /**
     * Iterate over the security sets that have the given clientMacAddress to find a certain security property.
     * If the given clientMacAddress is null, iterate over all security sets.
     * If the requested property is not defined on any security set, return null.
     */
    public String getSecurityKey(Device device, String propertyName, Integer clientMacAddress, DeviceMessage deviceMessage, String newKey) {
        List<SecurityPropertySet> securitySets = new ArrayList<>();
        for (SecurityPropertySet securityPropertySet : device.getConfiguration().getCommunicationConfiguration().getSecurityPropertySets()) {
            if (clientMacAddress == null) {
                securitySets.add(securityPropertySet);
            } else {
                for (SecurityProperty protocolSecurityProperty : device.getProtocolSecurityProperties(securityPropertySet)) {
                    //Only add this security set if it is for the given clientMacAddress
                    if (protocolSecurityProperty.getName().equals(SecurityPropertySpecName.CLIENT_MAC_ADDRESS.toString()) &&
                            ((BigDecimal) protocolSecurityProperty.getValue()).intValue() == clientMacAddress) {
                        securitySets.add(securityPropertySet);
                    }
                }
            }
        }

        for (SecurityPropertySet securityPropertySet : securitySets) {
            validateSecurityKeyLength(securityPropertySet, propertyName, newKey, deviceMessage);
            final List<SecurityProperty> securityProperties = device.getProtocolSecurityProperties(securityPropertySet);
            for (SecurityProperty securityProperty : securityProperties) {
                if (securityProperty.getName().equals(propertyName)) {
                    return (String) securityProperty.getValue();
                }
            }
        }
        return null;
    }

    protected ProtocolRuntimeException unsupportedPropertyValueWithReason(String propertyName, String propertyValue, String message) {
        return DeviceConfigurationException.unsupportedPropertyValueWithReason(propertyName, propertyValue, message);
    }

    protected ProtocolRuntimeException missingProperty(String propertyName) {
        return DeviceConfigurationException.missingProperty(propertyName);
    }

    protected ProtocolRuntimeException unsupportedPropertyValueLengthWithReason(String propertyName, String propertyValueLength, String reason) {
        return DeviceConfigurationException.unsupportedPropertyValueLengthWithReason(propertyName, propertyValueLength, reason);
    }

    private MeteringWarehouse getMeteringWarehouse() {
        final MeteringWarehouse meteringWarehouse = MeteringWarehouse.getCurrent();
        if (meteringWarehouse == null) {
            MeteringWarehouse.createBatchContext();
            return MeteringWarehouse.getCurrent();
        } else {
            return meteringWarehouse;
        }
    }
}
