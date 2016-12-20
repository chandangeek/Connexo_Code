package com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol;

import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifierType;

/**
 * Copyrights EnergyICT
 * Date: 8/4/14
 * Time: 12:09 PM
 */
public class TestSerialNumberDeviceIdentifier implements DeviceIdentifier {

    private final String serialNumber;

    public TestSerialNumberDeviceIdentifier(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    @Override
    public String getIdentifier() {
        return serialNumber;
    }

    @Override
    public DeviceIdentifierType getDeviceIdentifierType() {
        return DeviceIdentifierType.SerialNumber;
    }

    @Override
    public Device findDevice() {
        return null;
    }
}