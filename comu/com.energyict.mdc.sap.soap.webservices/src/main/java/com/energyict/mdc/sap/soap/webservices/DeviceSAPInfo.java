/*
 * Copyright (c) 2022 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices;

public class DeviceSAPInfo {
    String deviceId;
    String deviceLocation;
    String installationNumber;
    String pointOfDelivery;
    String divisionCategoryCode;
    String deviceLocationInformation;
    String modificationInformation;
    String activationGroupAmiFunctions;
    String meterFunctionGroup;
    String attributeMessage;
    String characteristicsId;
    String characteristicsValue;

    public DeviceSAPInfo() {
    }

    public String getDeviceLocation() {
        return deviceLocation;
    }

    public String getInstallationNumber() {
        return installationNumber;
    }

    public String getPointOfDelivery() {
        return pointOfDelivery;
    }

    public String getDivisionCategoryCode() {
        return divisionCategoryCode;
    }

    public String getDeviceLocationInformation() {
        return deviceLocationInformation;
    }

    public String getModificationInformation() {
        return modificationInformation;
    }

    public String getActivationGroupAmiFunctions() {
        return activationGroupAmiFunctions;
    }

    public String getMeterFunctionGroup() {
        return meterFunctionGroup;
    }

    public String getAttributeMessage() {
        return attributeMessage;
    }

    public String getCharacteristicsId() {
        return characteristicsId;
    }

    public String getCharacteristicsValue() {
        return characteristicsValue;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setDeviceLocation(String deviceLocation) {
        this.deviceLocation = deviceLocation;
    }

    public void setInstallationNumber(String installationNumber) {
        this.installationNumber = installationNumber;
    }

    public void setPointOfDelivery(String pointOfDelivery) {
        this.pointOfDelivery = pointOfDelivery;
    }

    public void setDivisionCategoryCode(String divisionCategoryCode) {
        this.divisionCategoryCode = divisionCategoryCode;
    }

    public void setDeviceLocationInformation(String deviceLocationInformation) {
        this.deviceLocationInformation = deviceLocationInformation;
    }

    public void setModificationInformation(String modificationInformation) {
        this.modificationInformation = modificationInformation;
    }

    public void setActivationGroupAmiFunctions(String activationGroupAmiFunctions) {
        this.activationGroupAmiFunctions = activationGroupAmiFunctions;
    }

    public void setMeterFunctionGroup(String meterFunctionGroup) {
        this.meterFunctionGroup = meterFunctionGroup;
    }

    public void setAttributeMessage(String attributeMessage) {
        this.attributeMessage = attributeMessage;
    }

    public void setCharacteristicsId(String characteristicsId) {
        this.characteristicsId = characteristicsId;
    }

    public void setCharacteristicsValue(String characteristicsValue) {
        this.characteristicsValue = characteristicsValue;
    }
}
