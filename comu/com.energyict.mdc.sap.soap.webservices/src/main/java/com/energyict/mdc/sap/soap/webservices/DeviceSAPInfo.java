package com.energyict.mdc.sap.soap.webservices;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.energyict.mdc.common.device.data.Device;

import aQute.bnd.annotation.ProviderType;

import java.util.Optional;

@ProviderType
public interface DeviceSAPInfo {

    Optional<String> getDeviceIdentifier();

    void setDeviceIdentifier(String deviceId);

    String getDeviceLocation();

    void setDeviceLocation(String deviceLocation);

    String getInstallationNumber();

    void setInstallationNumber(String installationNumber);

    String getPointOfDelivery();

    void setPointOfDelivery(String pointOfDelivery);

    String getDivisionCategoryCode();

    void setDivisionCategoryCode(String divisionCategoryCode);

    String getDeviceLocationInformation();

    void setDeviceLocationInformation(String deviceLocationInformation);

    String getModificationInformation();

    void setModificationInformation(String modificationInformation);

    String getActivationGroupAmiFunctions();

    void setActivationGroupAmiFunctions(String activationGroupAmiFunctions);

    String getMeterFunctionGroup();

    void setMeterFunctionGroup(String meterFunctionGroup);

    String getAttributeMessage();

    void setAttributeMessage(String attributeMessage);

    String getCharacteristicsId();

    void setCharacteristicsId(String characteristicsId);

    String getCharacteristicsValue();

    void setCharacteristicsValue(String characteristicsValue);

    void setRegisteredCustomPropertySet(RegisteredCustomPropertySet registeredCustomPropertySet);

    void setDevice(Device device);

    void save();
}
