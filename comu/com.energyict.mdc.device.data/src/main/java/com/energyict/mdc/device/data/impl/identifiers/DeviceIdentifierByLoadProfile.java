package com.energyict.mdc.device.data.impl.identifiers;

import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.meterdata.LoadProfile;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier;

import com.energyict.obis.ObisCode;

import javax.xml.bind.annotation.XmlElement;

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
    public Device findDevice() {
        com.energyict.mdc.device.data.LoadProfile loadProfile = (com.energyict.mdc.device.data.LoadProfile) this.loadProfileIdentifier.getLoadProfile();    //Downcast to Connexo LoadProfile
        return loadProfile.getDevice();
    }

    @Override
    public com.energyict.mdc.upl.meterdata.identifiers.Introspector forIntrospection() {
        return new Introspector();
    }

    @Override
    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    @Override
    public void setXmlType(String ignore) {

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
        public LoadProfile getLoadProfile() {
            throw new UnsupportedOperationException("NullLoadProfileIdentifier is not capable of finding a load profile there is not identifier");
        }

        @Override
        public ObisCode getProfileObisCode() {
            return null;
        }

        @Override
        public DeviceIdentifier getDeviceIdentifier() {
            return null;
        }

        @Override
        public com.energyict.mdc.upl.meterdata.identifiers.Introspector forIntrospection() {
            return new NullIntrospector();
        }

        @Override
        public String getXmlType() {
            return null;
        }

        @Override
        public void setXmlType(String ignore) {

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