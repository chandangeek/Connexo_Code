/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.identifiers;

/**
 * @author Stijn Vanhoorelbeke
 * @since 14.09.17 - 16:56
 */

import com.energyict.mdc.upl.io.ConnectionType;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.FindMultipleDevices;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
     * Constructor only to be used by JSON (de)marshalling or in unit tests
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
        return "device having connection type '" + this.connectionTypeClass.getSimpleName() + "', property '" + this.propertyName + "' and value '" + this.propertyValue + "'";
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
            return "ConnectionTypePropertyBased";
        }

        @Override
        public Set<String> getRoles() {
            return new HashSet<>(Arrays.asList("connectionTypeClass", "propertyName", "propertyValue"));
        }

        @Override
        public Object getValue(String role) {
            switch (role) {
                case "connectionTypeClass": {
                    return connectionTypeClass;
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

    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    public void setXmlType(String ignore) {
        // For xml unmarshalling purposes only
    }
}