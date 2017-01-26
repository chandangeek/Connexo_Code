package com.energyict.mdc.device.data.impl.identifiers;

import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.FindMultipleDevices;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Provides an implementation for the DeviceIdentifier interface,
 * The device can be found based on the given ConnectionType and a property value for that ConnectionType
 */
@XmlRootElement
public class DeviceIdentifierByConnectionTypeAndProperty implements DeviceIdentifier, FindMultipleDevices {

    private Class<? extends ConnectionType> connectionTypeClass;
    private String propertyName;
    private String propertyValue;

    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    public DeviceIdentifierByConnectionTypeAndProperty() {
    }

    public DeviceIdentifierByConnectionTypeAndProperty(Class<? extends ConnectionType> connectionTypeClass, String propertyName, String propertyValue) {
        this();
        this.connectionTypeClass = connectionTypeClass;
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
    }

    @Override
    public String toString() {
        return "device having connection type '" + this.connectionTypeClass.getName() + "', property '" + this.propertyName + "' and value '" + this.propertyValue + "'";
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
                case "connectionTypeClass": {
                    return connectionTypeClass;
                }
                case "connectionTypeClassName": {
                    return connectionTypeClass.getName();
                }
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