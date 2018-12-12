package com.energyict.protocolimplv2.messages.validators;

import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.security.SecurityPropertySet;
import com.energyict.protocolimplv2.security.SecurityPropertySpecTranslationKeys;

/**
 * Created by cisac on 11/25/2016.
 */
public class KeyMessageChangeValidator {
    public void validateNewKeyValue(Device device, DeviceMessage deviceMessage, SecurityPropertySpecTranslationKeys authenticationKey) {
        //TODO make compatible with UPL
    }

    public void validateNewKeyValueForFreeTextClient(long id, DeviceMessage deviceMessage, SecurityPropertySpecTranslationKeys authenticationKey) {

    }
/*
    *//**
     * Does a pre-verification of the new key
     *//*
    public void validateNewKeyValue(int deviceId, DeviceMessage deviceMessage, SecurityPropertySpecName securityPropertySpecName){
        int clientId = getClientId(deviceMessage);
        validateNewKeyValueForClient(deviceId, deviceMessage, securityPropertySpecName, clientId);
    }

    *//**
     * Does a pre-verification of the new key
     *//*
    public void validateNewKeyValueForFreeTextClient(int deviceId, DeviceMessage deviceMessage, SecurityPropertySpecName securityPropertySpecName){
        int clientId = getFreeTextClientId(deviceMessage);
        validateNewKeyValueForClient(deviceId, deviceMessage, securityPropertySpecName, clientId);
    }

    *//**
     * Does a pre-verification of the new key
     *//*
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
    } */

    public void validateSecurityKeyLength(SecurityPropertySet securityPropertySet, String propertyName, String key, DeviceMessage deviceMessage) {
//        int securitySuite = securityPropertySet.getSecuritySuiteId();
//        int suite2Length = 64;
//        int defaultLength = 32;
//
//        if(securitySuite == 2 && key.length() != suite2Length){
//            String failedReason = "The length of the security key is incorrect. Expected " + suite2Length + " but was " + key.length();
//            deviceMessage.fail(failedReason);
//            unsupportedPropertyValueLengthWithReason(propertyName, String.valueOf(suite2Length), failedReason);
//        } else if(key.length() != defaultLength) {
//            //for other suites use defaultLength
//            String failedReason = "The length of the security key is incorrect. Expected " + defaultLength + " but was " + key.length();
//            deviceMessage.fail(failedReason);
//            unsupportedPropertyValueLengthWithReason(propertyName, String.valueOf(defaultLength), failedReason);
//        }
    }

/*    private String getAttributeNameForSecurityProperty(SecurityPropertySpecName securityPropertySpecName){
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
        DeviceMessageAttribute clientMacAttribute = getDeviceMessageAttribute(deviceMessage, DeviceMessageConstants.clientMacAddress);

        if (clientMacAttribute.getValue() instanceof BigDecimal){
            BigDecimal client = (BigDecimal) clientMacAttribute.getValue();
            if (client!=null){
                return client.intValue();
            }
        }

        if (clientMacAttribute.getValue() instanceof String){
            String client = (String) clientMacAttribute.getValue();
            if (client != null) {
                if (!client.isEmpty()) {
                    try {
                        return Integer.parseInt(client);
                    } catch (Exception ex) {
                        // swallow
                    }
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

    *//**
     * Iterate over the security sets that have the given clientMacAddress to find a certain security property.
     * If the given clientMacAddress is null, iterate over all security sets.
     * If the requested property is not defined on any security set, return null.
     *//*
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
    }*/
}
