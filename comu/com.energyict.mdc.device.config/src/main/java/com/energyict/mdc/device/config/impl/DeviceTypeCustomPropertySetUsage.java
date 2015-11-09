package com.energyict.mdc.device.config.impl;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.energyict.mdc.device.config.DeviceType;

@ProviderType
public interface DeviceTypeCustomPropertySetUsage {

    DeviceType getDeviceType();

    RegisteredCustomPropertySet getRegisteredCustomPropertySet();
}