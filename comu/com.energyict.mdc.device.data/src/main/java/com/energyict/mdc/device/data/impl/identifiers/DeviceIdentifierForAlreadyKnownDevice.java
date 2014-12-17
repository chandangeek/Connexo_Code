package com.energyict.mdc.device.data.impl.identifiers;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifierType;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
* Copyrights EnergyICT
* Date: 8/1/14
* Time: 8:50 AM
*/
@XmlRootElement
public final class DeviceIdentifierForAlreadyKnownDevice implements DeviceIdentifier<Device> {

    private Device device;

    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    public DeviceIdentifierForAlreadyKnownDevice() {
    }

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
