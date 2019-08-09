/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine;

import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.masterdata.LoadProfileType;
import com.energyict.mdc.common.masterdata.LogBookType;
import com.energyict.mdc.common.masterdata.RegisterType;

import java.time.Instant;

public interface DeviceBuilderForTesting {
    DeviceBuilderForTesting name(String name);
    DeviceBuilderForTesting mRDI(String mRDI);
    DeviceBuilderForTesting registerType(RegisterType registerType);
    DeviceBuilderForTesting loadProfileTypes(LoadProfileType... loadProfilesTypes);
    DeviceBuilderForTesting logBookTypes(LogBookType... logBooks);
    DeviceBuilderForTesting deviceTypeName(String deviceTypeName);
    DeviceBuilderForTesting deviceConfigName(String deviceConfigName);
    DeviceBuilderForTesting dataLoggerEnabled(boolean enabled);
    DeviceBuilderForTesting multiElementEnabled(boolean enabled);
    DeviceBuilderForTesting dataLoggerSlaveDevice();
    DeviceBuilderForTesting multiElementSlaveDevice();
    Device create(Instant when);
}
