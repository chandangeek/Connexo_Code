package com.energyict.mdc.engine.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifierType;

import javax.xml.bind.annotation.XmlElement;

/**
* Copyrights EnergyICT
* Date: 8/1/14
* Time: 8:50 AM
*/
public final class DeviceIdentifierForAlreadyKnownDevice implements DeviceIdentifier<Device> {

    private final Device device;

    public DeviceIdentifierForAlreadyKnownDevice(Device device) {
        this.device = device;
    }

    @Override
    public String getIdentifier() {
        return this.device.getmRID();
    }

    @Override
    public DeviceIdentifierType getDeviceIdentifierType() {
        return DeviceIdentifierType.ActualDevice;
    }

    @Override
    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    @Override
    public void setXmlType(String ignore) {

    }

    @Override
    public Device findDevice() {
        return this.device;
    }
}
