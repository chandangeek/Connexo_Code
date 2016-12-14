package com.energyict.mdc.device.data.impl.identifiers;

import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This is a DeviceIdentifier that uniquely identifies a Device which you have given in the Constructor.
 *
 * <b>You are encouraged to only use this identifier within the ComServer engine. If we provide this
 * identifier to a protocol, then the returned MRId by the Introspector is in most cases unknown to the device.
 * It is better to use the DeviceIdentifierForAlreadyKnownDeviceBySerialNumber in that case.</b>
 */
@XmlRootElement
public final class DeviceIdentifierForAlreadyKnownDeviceByMrID implements DeviceIdentifier {

    private Device device;

    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    public DeviceIdentifierForAlreadyKnownDeviceByMrID() {
    }

    public DeviceIdentifierForAlreadyKnownDeviceByMrID(Device device) {
        this();
        this.device = device;
    }

    @Override
    public com.energyict.mdc.upl.meterdata.identifiers.Introspector forIntrospection() {
        return new Introspector();
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
        return "device having MRID " + getmRID();
    }

    private String getmRID() {
        return ((com.energyict.mdc.device.data.Device) device).getmRID();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DeviceIdentifierForAlreadyKnownDeviceByMrID that = (DeviceIdentifierForAlreadyKnownDeviceByMrID) o;

        return ((com.energyict.mdc.device.data.Device) this.device).getId() == ((com.energyict.mdc.device.data.Device) that.device).getId();

    }

    @Override
    public int hashCode() {
        return device.hashCode();
    }

    private class Introspector implements com.energyict.mdc.upl.meterdata.identifiers.Introspector {
        @Override
        public String getTypeName() {
            return "Actual";
        }

        @Override
        public Object getValue(String role) {
            switch (role) {
                case "actual": {
                    return device;
                }
                case "mRID": {
                    return getmRID();
                }
                default: {
                    throw new IllegalArgumentException("Role '" + role + "' is not supported by identifier of type " + getTypeName());
                }
            }
        }

    }

}