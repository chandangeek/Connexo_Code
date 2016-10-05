package com.energyict.mdc.device.data.validation;


public interface ValidationOverview {

    String getDeviceName();

    void setDeviceName(String name);

    String getSerialNumber();

    void setSerialNumber(String serialNumber);

    String getDeviceType();

    void setDeviceType(String deviceType);

    String getDeviceConfig();

    void setDeviceConfig(String deviceConfig);

    DeviceValidationKpiResults getDeviceValidationKpiResults();

    void setDeviceValidationKpiResults(DeviceValidationKpiResults deviceValidationKpiResults);
}
