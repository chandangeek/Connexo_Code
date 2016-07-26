package com.energyict.mdc.device.data.validation;


public interface ValidationOverview {

    String getMrid();

    void setMrid(String mrid);

    String getSerialNumber();

    void setSerialNumber(String serialNumber);

    String getDeviceType();

    void setDeviceType(String deviceType);

    String getDeviceConfig();

    void setDeviceConfig(String deviceConfig);

    DeviceValidationKpiResults getDeviceValidationKpiResults();

    void setDeviceValidationKpiResults(DeviceValidationKpiResults deviceValidationKpiResults);
}
