package com.energyict.mdc.sap.soap.webservices;

import aQute.bnd.annotation.ProviderType;

import java.util.Optional;

@ProviderType
public interface SapDeviceInfo {

    Optional<String> getDeviceIdentifier();

    void setDeviceIdentifier(String deviceId);

    Optional<String> getDeviceLocation();

    void setDeviceLocation(String deviceLocation);

    Optional<String> getInstallationNumber();

    void setInstallationNumber(String installationNumber);

    Optional<String> getPointOfDelivery();

    void setPointOfDelivery(String pointOfDelivery);

    Optional<String> getDivisionCategoryCode();

    void setDivisionCategoryCode(String divisionCategoryCode);

    Optional<String> getDeviceLocationInformation();

    void setDeviceLocationInformation(String deviceLocationInformation);

    Optional<String> getModificationInformation();

    void setModificationInformation(String modificationInformation);

    Optional<String> getActivationGroupAmiFunctions();

    void setActivationGroupAmiFunctions(String activationGroupAmiFunctions);

    Optional<String> getMeterFunctionGroup();

    void setMeterFunctionGroup(String meterFunctionGroup);

    Optional<String> getAttributeMessage();

    void setAttributeMessage(String attributeMessage);

    Optional<String> getCharacteristicsId();

    void setCharacteristicsId(String characteristicsId);

    Optional<String> getCharacteristicsValue();

    void setCharacteristicsValue(String characteristicsValue);

    void save();
}
