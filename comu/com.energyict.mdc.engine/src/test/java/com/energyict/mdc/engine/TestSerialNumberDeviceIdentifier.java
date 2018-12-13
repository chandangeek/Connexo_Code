/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine;

import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.Introspector;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class TestSerialNumberDeviceIdentifier implements DeviceIdentifier {

    private final String serialNumber;

    public TestSerialNumberDeviceIdentifier(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    @Override
    public Introspector forIntrospection() {
        return new TestIntrospector();
    }

    private class TestIntrospector implements Introspector {
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