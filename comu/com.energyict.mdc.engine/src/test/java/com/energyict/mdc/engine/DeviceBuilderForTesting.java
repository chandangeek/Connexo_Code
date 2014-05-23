package com.energyict.mdc.engine;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LogBookType;

/**
* Copyrights EnergyICT
* Date: 22/05/14
* Time: 15:55
*/
public interface DeviceBuilderForTesting {
    DeviceBuilderForTesting name(String name);
    DeviceBuilderForTesting mRDI(String mRDI);
    DeviceBuilderForTesting loadProfileTypes(LoadProfileType... loadProfilesTypes);
    DeviceBuilderForTesting logBookTypes(LogBookType... logBooks);
    Device create();
}
