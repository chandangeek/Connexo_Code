package com.energyict.protocolimplv2.identifiers;

import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.FindMultipleDevices;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Provides an implementation for the {@link DeviceIdentifier} interface
 * that uses the Device's serial number to uniquely identify it.
 * <p/>
 * Wild cards can be used in the serial number.
 * E.g. *012345* matches 660-012345-1245
 * <p/>
 * Copyrights EnergyICT
 * Date: 13/05/13
 * Time: 13:06
 */
@XmlRootElement
public class DeviceIdentifierLikeSerialNumber implements FindMultipleDevices {

    private String serialNumber;

    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    private DeviceIdentifierLikeSerialNumber() {
    }

    public DeviceIdentifierLikeSerialNumber(String serialNumber) {
        super();
        this.serialNumber = serialNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeviceIdentifierLikeSerialNumber that = (DeviceIdentifierLikeSerialNumber) o;
        return serialNumber.equals(that.serialNumber);
    }

    @Override
    public int hashCode() {
        return serialNumber.hashCode();
    }

    @Override
    public String toString() {
        return "device with serial number like " + this.serialNumber;
    }

    @XmlAttribute
    public String getSerialNumber() {
        return serialNumber;
    }

    @Override
    public com.energyict.mdc.upl.meterdata.identifiers.Introspector forIntrospection() {
        return new Introspector();
    }

    private class Introspector implements com.energyict.mdc.upl.meterdata.identifiers.Introspector {
        @Override
        public String getTypeName() {
            return "LikeSerialNumber";
        }

        @Override
        public Object getValue(String role) {
            if ("serialNumberGrepPattern".equals(role)) {
                return serialNumber;
            } else {
                throw new IllegalArgumentException("Role '" + role + "' is not supported by identifier of type " + getTypeName());
            }
        }

    }

}
