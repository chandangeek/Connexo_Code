/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.devices.topology;

import com.energyict.mdc.device.data.importers.impl.FileImportRecord;

public class DeviceTopologyImportRecord extends FileImportRecord {
    private String masterDeviceIdentifier;
    private String slaveDeviceIdentifier;

    public String getMasterDeviceIdentifier() {
        return masterDeviceIdentifier;
    }

    public void setMasterDeviceIdentifier(String masterDeviceIdentifier) {
        this.masterDeviceIdentifier = masterDeviceIdentifier;
    }

    public String getSlaveDeviceIdentifier() {
        return slaveDeviceIdentifier;
    }

    public void setSlaveDeviceIdentifier(String slaveDeviceIdentifier) {
        this.slaveDeviceIdentifier = slaveDeviceIdentifier;
    }
}
