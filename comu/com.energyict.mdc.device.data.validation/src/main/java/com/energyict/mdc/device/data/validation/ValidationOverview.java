package com.energyict.mdc.device.data.validation;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface ValidationOverview {

    String getDeviceName();

    String getDeviceSerialNumber();

    String getDeviceTypeName();

    String getDeviceConfigurationName();

    DeviceValidationKpiResults getDeviceValidationKpiResults();

}