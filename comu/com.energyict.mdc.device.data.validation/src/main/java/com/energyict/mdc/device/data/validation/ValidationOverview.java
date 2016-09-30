package com.energyict.mdc.device.data.validation;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface ValidationOverview {

    String getDevice_mRID();

    String getDeviceSerialNumber();

    String getDeviceTypeName();

    String getDeviceConfigurationName();

    DeviceValidationKpiResults getDeviceValidationKpiResults();

}