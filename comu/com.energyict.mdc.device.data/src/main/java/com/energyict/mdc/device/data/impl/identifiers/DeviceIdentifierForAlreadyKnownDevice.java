/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.identifiers;

import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * This is a DeviceIdentifier that uniquely identifies a Device which you have given in the Constructor.
 */
@XmlRootElement
public final class DeviceIdentifierForAlreadyKnownDevice implements DeviceIdentifier {

    private Device device;

    /**
     * Constructor only to be used by JSON (de)marshalling or in unit tests
     */
    public DeviceIdentifierForAlreadyKnownDevice() {
    }

    public DeviceIdentifierForAlreadyKnownDevice(Device device) {
        this();
        this.device = device;
    }

    @Override
    public com.energyict.mdc.upl.meterdata.identifiers.Introspector forIntrospection() {
        return new Introspector();
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

        DeviceIdentifierForAlreadyKnownDevice that = (DeviceIdentifierForAlreadyKnownDevice) o;

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
        public Set<String> getRoles() {
            return new HashSet<>(Arrays.asList("actual", "mRID"));
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