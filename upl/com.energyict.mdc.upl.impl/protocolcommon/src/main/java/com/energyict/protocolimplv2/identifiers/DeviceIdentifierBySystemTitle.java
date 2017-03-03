/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.identifiers;

import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.FindMultipleDevices;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Provides an implementation for the {@link DeviceIdentifier} interface
 * that uses an {@link Device}'s system title to uniquely identify it.
 *
 * @author sva
 * @since 26/10/12 (11:26)
 */
@XmlRootElement
public class DeviceIdentifierBySystemTitle implements FindMultipleDevices {

    private final String systemTitle;

    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    private DeviceIdentifierBySystemTitle() {
        systemTitle = "";
    }

    public DeviceIdentifierBySystemTitle(String systemTitle) {
        super();
        this.systemTitle = systemTitle;
    }

    @Override
    public String toString() {
        return "device with system title " + this.systemTitle;
    }

    @XmlAttribute
    public String getSystemTitle() {
        return systemTitle;
    }

    @Override
    public com.energyict.mdc.upl.meterdata.identifiers.Introspector forIntrospection() {
        return new Introspector();
    }

    private class Introspector implements com.energyict.mdc.upl.meterdata.identifiers.Introspector {
        @Override
        public String getTypeName() {
            return "systemTitle";
        }

        @Override
        public Set<String> getRoles() {
            return new HashSet<>(Collections.singletonList("systemTitle"));
        }

        @Override
        public Object getValue(String role) {
            if ("systemTitle".equals(role)) {
                return systemTitle;
            } else {
                throw new IllegalArgumentException("Role '" + role + "' is not supported by identifier of type " + getTypeName());
            }
        }
    }
}