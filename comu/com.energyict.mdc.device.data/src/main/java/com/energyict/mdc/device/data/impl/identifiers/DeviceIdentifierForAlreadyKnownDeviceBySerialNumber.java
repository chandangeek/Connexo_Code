package com.energyict.mdc.device.data.impl.identifiers;

import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifierType;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This is a DeviceIdentifier that uniquely identifies a Device which you have given in the Constructor.
 * The {@link #getIdentifier()} method will return the <b>SerialNumber</b> of the device!
 */
@XmlRootElement
public class DeviceIdentifierForAlreadyKnownDeviceBySerialNumber implements DeviceIdentifier {

    private Device device;

    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    public DeviceIdentifierForAlreadyKnownDeviceBySerialNumber() {
    }

    public DeviceIdentifierForAlreadyKnownDeviceBySerialNumber(Device device) {
        this.device = device;
    }

    /**
     * @return the SERIALNUMBER of the device
     */
    @Override
    public String getIdentifier() {
        return ((com.energyict.mdc.device.data.Device) this.device).getSerialNumber();
    }

    @Override
    public DeviceIdentifierType getDeviceIdentifierType() {
        return DeviceIdentifierType.SerialNumber;
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

    @Override
    public String toString() {
        return "device having serial number '" + ((com.energyict.mdc.device.data.Device) this.device).getSerialNumber() + "'";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DeviceIdentifierForAlreadyKnownDeviceBySerialNumber that = (DeviceIdentifierForAlreadyKnownDeviceBySerialNumber) o;

        return ((com.energyict.mdc.device.data.Device) this.device).getId() == ((com.energyict.mdc.device.data.Device) that.device).getId();

    }

    @Override
    public int hashCode() {
        return device.hashCode();
    }
}