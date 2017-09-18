package com.energyict.protocolimplv2.identifiers;

import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.FindMultipleDevices;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Provides an implementation for the {@link DeviceIdentifier} interface
 * that uses the Device's serial number to uniquely identify it.
 *
 * Copyrights EnergyICT
 * Date: 13/05/13
 * Time: 13:06
 */
@XmlRootElement
public class DeviceIdentifierBySerialNumber implements FindMultipleDevices {

    private String serialNumber;

    /**
     * Constructor only to be used by JSON (de)marshalling or in unit tests
     */
    public DeviceIdentifierBySerialNumber() {
    }

    public DeviceIdentifierBySerialNumber(String serialNumber) {
        this();
        this.serialNumber = serialNumber;
    }

    @Override
    public boolean equals (Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof DeviceIdentifierBySerialNumber) {
            DeviceIdentifierBySerialNumber that = (DeviceIdentifierBySerialNumber) o;
            return serialNumber.equals(that.serialNumber);
        } else if (o instanceof DeviceIdentifier) {
            DeviceIdentifier that = (DeviceIdentifier) o;
            com.energyict.mdc.upl.meterdata.identifiers.Introspector introspector = that.forIntrospection();
            if ("SerialNumber".equals(introspector.getTypeName())) {
                return this.serialNumber.equals(introspector.getValue("serialNumber"));
            } else {
                try {
                	return this.serialNumber.equals(introspector.getValue("SerialNumber"));
                }
                catch (IllegalArgumentException e) {
                    // Ok, so the DeviceIdentifier really does not support serial numbers
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    public boolean equalsIgnoreCase (DeviceIdentifier other) {
        if (this == other) {
            return true;
        }
        if (other instanceof DeviceIdentifierBySerialNumber) {
            DeviceIdentifierBySerialNumber that = (DeviceIdentifierBySerialNumber) other;
            return serialNumber.equalsIgnoreCase(that.serialNumber);
        } else {
            com.energyict.mdc.upl.meterdata.identifiers.Introspector introspector = other.forIntrospection();
            if ("SerialNumber".equals(introspector.getTypeName())) {
                return this.serialNumber.equalsIgnoreCase(String.valueOf(introspector.getValue("serialNumber")));
            } else {
                try {
                	return this.serialNumber.equalsIgnoreCase(String.valueOf(introspector.getValue("SerialNumber")));
                }
                catch (IllegalArgumentException e) {
                    // Ok, so the DeviceIdentifier really does not support serial numbers
                    return false;
                }
            }
        }
    }

    @Override
    public int hashCode () {
        return serialNumber.hashCode();
    }

    @Override
    public String toString () {
        return "device with serial number " + this.serialNumber;
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
            return "SerialNumber";
        }

        @Override
        public Set<String> getRoles() {
            return new HashSet<>(Collections.singletonList("serialNumber"));
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