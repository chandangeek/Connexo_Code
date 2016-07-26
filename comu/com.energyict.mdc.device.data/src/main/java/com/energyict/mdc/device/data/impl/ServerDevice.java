package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.orm.associations.Reference;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.sync.SyncDeviceWithKoreForInfo;

/**
 * Copyrights EnergyICT
 * Date: 26.07.16
 * Time: 14:15
 */
public interface ServerDevice extends Device {
    Reference<Meter> getMeter();

    SyncDeviceWithKoreForInfo getKoreHelper();
}
