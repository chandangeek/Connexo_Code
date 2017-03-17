package com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol;

import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Copyrights EnergyICT
 * Date: 8/4/14
 * Time: 12:09 PM
 */
public class TestSerialNumberDeviceIdentifier implements DeviceIdentifier {

    private final String serialNumber;

    public TestSerialNumberDeviceIdentifier(String serialNumber) {
        this.serialNumber = serialNumber;
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