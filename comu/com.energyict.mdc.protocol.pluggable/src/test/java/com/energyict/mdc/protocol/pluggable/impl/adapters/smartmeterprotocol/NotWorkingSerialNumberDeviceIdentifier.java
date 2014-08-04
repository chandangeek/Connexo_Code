package com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol;

import com.energyict.mdc.protocol.api.device.BaseChannel;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.BaseLoadProfile;
import com.energyict.mdc.protocol.api.device.BaseRegister;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;

/**
* Copyrights EnergyICT
* Date: 8/4/14
* Time: 12:09 PM
*/
public class NotWorkingSerialNumberDeviceIdentifier implements DeviceIdentifier {

    private final String serialNumber;

    public NotWorkingSerialNumberDeviceIdentifier(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    @Override
    public String getIdentifier() {
        return serialNumber;
    }

    @Override
    public BaseDevice<? extends BaseChannel, ? extends BaseLoadProfile<? extends BaseChannel>, ? extends BaseRegister> findDevice() {
        return null;
    }
}
