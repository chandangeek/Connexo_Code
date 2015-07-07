package com.energyict.mdc.device.data.impl.identifiers;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifierType;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This is a DeviceIdentifier that uniquely identifies a Device which you have given in the Constructor.
 * The {@link #getIdentifier()} method will return the <b>MRId</b> of the device!
 *
 * <b>You are encouraged to only use this identifier within the ComServer engine. If we provide this
 * identifier to a protocol, then the returned MRId from {@link #getIdentifier()} is in most cases unknown to the device.
 * It is better to use the DeviceIdentifierForAlreadyKnownDeviceBySerialNumber for that.</b>
 */
@XmlRootElement
public final class DeviceIdentifierForAlreadyKnownDeviceByMrID implements DeviceIdentifier<Device> {

    private Device device;

    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    public DeviceIdentifierForAlreadyKnownDeviceByMrID() {
    }

    public DeviceIdentifierForAlreadyKnownDeviceByMrID(Device device) {
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
