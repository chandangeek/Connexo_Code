package com.energyict.mdc.engine;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.RegisterType;

import java.time.Instant;

/**
* Copyrights EnergyICT
* Date: 22/05/14
* Time: 15:55
*/
public interface DeviceBuilderForTesting {
    DeviceBuilderForTesting name(String name);
    DeviceBuilderForTesting mRDI(String mRDI);
    DeviceBuilderForTesting registerType(RegisterType registerType);
    DeviceBuilderForTesting loadProfileTypes(LoadProfileType... loadProfilesTypes);
    DeviceBuilderForTesting logBookTypes(LogBookType... logBooks);
    DeviceBuilderForTesting deviceTypeName(String deviceTypeName);
    DeviceBuilderForTesting deviceConfigName(String deviceConfigName);
    DeviceBuilderForTesting dataLoggerEnabled(boolean enabled);
    DeviceBuilderForTesting dataLoggerSlaveDevice();
    Device create(Instant when);
}
