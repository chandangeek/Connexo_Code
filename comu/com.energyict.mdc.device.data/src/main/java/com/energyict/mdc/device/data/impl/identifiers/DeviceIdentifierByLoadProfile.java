package com.energyict.mdc.device.data.impl.identifiers;

import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier;

import com.energyict.obis.ObisCode;

/**
 * Copyrights EnergyICT
 * Date: 2/23/15
 * Time: 3:11 PM
 */
public class DeviceIdentifierByLoadProfile implements DeviceIdentifier {

    private final LoadProfileIdentifier loadProfileIdentifier;

    // JSON demarshalling purpose only
    @SuppressWarnings("unused")
    public DeviceIdentifierByLoadProfile() {
        this(new NullLoadProfileIdentifier());
    }

    public DeviceIdentifierByLoadProfile(LoadProfileIdentifier loadProfileIdentifier) {
        this.loadProfileIdentifier = loadProfileIdentifier;
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
        if (!(o instanceof DeviceIdentifierByLoadProfile)) {
            return false;
        }

        DeviceIdentifierByLoadProfile that = (DeviceIdentifierByLoadProfile) o;

        return loadProfileIdentifier.equals(that.loadProfileIdentifier);

    }

    @Override
    public int hashCode() {
        return loadProfileIdentifier.hashCode();
    }

    @Override
    public String toString() {
        return "device having load profile identified by '" + this.loadProfileIdentifier + "'";
    }

    private class Introspector implements com.energyict.mdc.upl.meterdata.identifiers.Introspector {
        @Override
        public String getTypeName() {
            return "Other";
        }

        @Override
        public Object getValue(String role) {
            return loadProfileIdentifier.forIntrospection().getValue(role);
        }
    }

    private static class NullLoadProfileIdentifier implements LoadProfileIdentifier {
        @Override
        public ObisCode getProfileObisCode() {
            return null;
        }

        @Override
        public com.energyict.mdc.upl.meterdata.identifiers.Introspector forIntrospection() {
            return new NullIntrospector();
        }
    }

    private static class NullIntrospector implements com.energyict.mdc.upl.meterdata.identifiers.Introspector {
        @Override
        public String getTypeName() {
            return "Null";
        }

        @Override
        public Object getValue(String role) {
            throw new IllegalArgumentException("Role '" + role + "' is not supported by identifier of type " + getTypeName());
        }
    }

}