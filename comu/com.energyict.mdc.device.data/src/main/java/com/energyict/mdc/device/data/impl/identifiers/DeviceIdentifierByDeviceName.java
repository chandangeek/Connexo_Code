/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.identifiers;

import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Provides an implementation for the {@link DeviceIdentifier} interface
 * that uses the unique device name of the device
 *
 */
@XmlRootElement
public final class DeviceIdentifierByDeviceName implements DeviceIdentifier {

    private String name;

    /**
     * Constructor only to be used by JSON (de)marshalling or in unit tests
     */
    public DeviceIdentifierByDeviceName() {
    }

    public DeviceIdentifierByDeviceName(String name) {
        this();
        this.name = name;
    }

    @Override
    public String toString() {
        return "device with the name " + this.name;
    }

    @Override
    public com.energyict.mdc.upl.meterdata.identifiers.Introspector forIntrospection() {
        return new Introspector();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DeviceIdentifierByDeviceName)) {
            return false;
        }

        DeviceIdentifierByDeviceName that = (DeviceIdentifierByDeviceName) o;

        return !(name != null ? !name.equals(that.name) : that.name != null);

    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    private class Introspector implements com.energyict.mdc.upl.meterdata.identifiers.Introspector {
        @Override
        public String getTypeName() {
            return "Name";
        }

        @Override
        public Set<String> getRoles() {
            return new HashSet<>(Collections.singletonList("databaseValue"));
        }

        @Override
        public Object getValue(String role) {
            switch (role) {
                case "databaseValue": {
                    return name;
                }
                default: {
                    throw new IllegalArgumentException("Role '" + role + "' is not supported by identifier of type " + getTypeName());
                }
            }
        }
    }
}
