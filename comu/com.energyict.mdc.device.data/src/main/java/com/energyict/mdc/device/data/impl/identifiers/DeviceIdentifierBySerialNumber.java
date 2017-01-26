package com.energyict.mdc.device.data.impl.identifiers;

import com.energyict.mdc.protocol.api.exceptions.DuplicateException;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.FindMultipleDevices;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Provides an implementation for the {@link DeviceIdentifier} interface
 * that uses an {@link com.energyict.mdc.upl.meterdata.Device}'s serial number to uniquely identify it.
 * <br/><br/>
 * <b>NOTE:</b> It is strongly advised to use the {@link DeviceIdentifierById} instead of this one.
 * The SerialNumber of a device doesn't have to be unique. If this identifier finds more than one
 * device with the same serialNumber, then a {@link DuplicateException} will be thrown indicating that.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-12 (11:16)
 */
@XmlRootElement
public class DeviceIdentifierBySerialNumber implements DeviceIdentifier, FindMultipleDevices {

    private String serialNumber;

    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    public DeviceIdentifierBySerialNumber() {
    }

    public DeviceIdentifierBySerialNumber(String serialNumber) {
        this();
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
        DeviceIdentifierBySerialNumber that = (DeviceIdentifierBySerialNumber) o;
        return serialNumber.equals(that.serialNumber);
    }

    @Override
    public int hashCode() {
        return serialNumber.hashCode();
    }

    @Override
    public String toString() {
        return "device having serial number '" + this.serialNumber + "'";
    }

    @Override
    public com.energyict.mdc.upl.meterdata.identifiers.Introspector forIntrospection() {
        return new Introspector();
    }

    private class Introspector implements com.energyict.mdc.upl.meterdata.identifiers.Introspector {
        @Override
        public String getTypeName() {
            return "SerialNumber";
        }

        @Override
        public Object getValue(String role) {
            if ("serialNumber".equals(role)) {
                return serialNumber;
            } else {
                throw new IllegalArgumentException("Role '" + role + "' is not supported by identifier of type " + getTypeName());
            }
        }
    }
}