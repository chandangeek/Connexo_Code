/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.identifiers;

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
 * The device can be found based on the given property name and property value.
 * Note that multiple devices can be found with the provided combinations
 */
@XmlRootElement
public class DeviceIdentifierByPropertyValue implements DeviceIdentifier, FindMultipleDevices {

    private String propertyName;
    private String propertyValue;

    /**
     * Constructor only to be used by JSON (de)marshalling or in unit tests
     */
    public DeviceIdentifierByPropertyValue() {
    }

    public DeviceIdentifierByPropertyValue(String propertyName, String propertyValue) {
        this();
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
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
        public Set<String> getRoles() {
            return new HashSet<>(Arrays.asList("propertyName", "propertyValue"));
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

    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    public void setXmlType(String ignore) {
        // For xml unmarshalling purposes only
    }
}