package com.energyict.mdc.sap.soap.webservices;

import com.elster.jupiter.cps.CustomPropertySetValues;

public interface DeviceSAPInfo {


    void setDeviceIdentifier(String deviceId);

    void setDeviceLocation(String deviceLocation);

    void setInstallationNumber(String installationNumber);

    void setPointOfDelivery(String pointOfDelivery);

    void setDivisionCategoryCode(String divisionCategoryCode);

    void setDeviceLocationInformation(String deviceLocationInformation);

    void setModificationInformation(String modificationInformation);

    void setActivationGroupAmiFunctions(String activationGroupAmiFunctions);

    void setMeterFunctionGroup(String meterFunctionGroup);

    void setAttributeMessage(String attributeMessage);

    void setCharacteristicsId(String characteristicsId);

    void setCharacteristicsValue(String characteristicsValue);

    CustomPropertySetValues getCustomPropertySetValue();
}
