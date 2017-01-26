package com.energyict.mdc.device.data.impl.identifiers;

import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.FindMultipleDevices;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Provides an implementation for the DeviceIdentifier interface,
 * The device can be found based on the given propertyname and propertyvalue.
 * Note that multiple devices can be found with the provided combinations
 */
@XmlRootElement
public class DeviceIdentifierByPropertyValue implements DeviceIdentifier, FindMultipleDevices {

    private String propertyName;
    private String propertyValue;

    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    public DeviceIdentifierByPropertyValue() {
    }

    public DeviceIdentifierByPropertyValue(String propertyName, String callHomeId) {
        this();
        this.propertyName = propertyName;
        this.propertyValue = callHomeId;
    }

    @Override
    public String toString() {
        return "device having property '" + this.propertyName + "' and value '" + this.propertyValue + "'";
    }

    @XmlAttribute
    public String getPropertyValue() {
        return propertyValue;
    }

    @Override
    public com.energyict.mdc.upl.meterdata.identifiers.Introspector forIntrospection() {
        return new Introspector();
    }

    private class Introspector implements com.energyict.mdc.upl.meterdata.identifiers.Introspector {
        @Override
        public String getTypeName() {
            return "PropertyBased";
        }

        @Override
        public Object getValue(String role) {
            switch (role) {
                case "propertyName": {
                    return propertyName;
                }
                case "propertyValue": {
                    return propertyValue;
                }
                default: {
                    throw new IllegalArgumentException("Role '" + role + "' is not supported by identifier of type " + getTypeName());
                }
            }
        }
    }
}