package com.energyict.mdc.engine;

import com.energyict.mdc.protocol.api.device.BaseChannel;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.BaseLoadProfile;
import com.energyict.mdc.protocol.api.device.BaseRegister;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifierType;

/**
 * Copyrights EnergyICT
 * Date: 8/4/14
 * Time: 1:43 PM
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
    public String getXmlType() {
        return null;
    }

    @Override
    public void setXmlType(String ignore) {

    }

    @Override
    public BaseDevice<? extends BaseChannel, ? extends BaseLoadProfile<? extends BaseChannel>, ? extends BaseRegister> findDevice() {
        return null;
    }

}