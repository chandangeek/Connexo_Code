package com.energyict.mdc.device.data.impl.identifiers;

import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier;

import com.energyict.obis.ObisCode;

import java.text.MessageFormat;

/**
 * Copyrights EnergyICT
 * Date: 8/1/14
 * Time: 2:59 PM
 */
public class LoadProfileIdentifierForAlreadyKnownLoadProfile implements LoadProfileIdentifier {

    private final LoadProfile loadProfile;
    private final ObisCode profileObisCode;

    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    @SuppressWarnings("unused")
    public LoadProfileIdentifierForAlreadyKnownLoadProfile() {
        super();
        this.loadProfile = null;
        this.profileObisCode = null;
    }

    public LoadProfileIdentifierForAlreadyKnownLoadProfile(LoadProfile loadProfile, ObisCode obisCode) {
        this.loadProfile = loadProfile;
        this.profileObisCode = obisCode;
    }

    @Override
    public ObisCode getProfileObisCode() {
        return profileObisCode;
    }

    @Override
    public LoadProfile getLoadProfile() {
        return this.loadProfile;
    }

    @Override
    public com.energyict.mdc.upl.meterdata.identifiers.Introspector forIntrospection() {
        return new Introspector();
    }

    @Override
    public DeviceIdentifier getDeviceIdentifier() {
        return new DeviceIdentifierForAlreadyKnownDeviceByMrID(this.loadProfile.getDevice());
    }

    @Override
    public String toString() {
        return MessageFormat.format("load profile with name ''{0}'' on device having MRID {1}", loadProfile.getLoadProfileSpec().getLoadProfileType().getName(), loadProfile.getId());
    }

    private class Introspector implements com.energyict.mdc.upl.meterdata.identifiers.Introspector {
        @Override
        public String getTypeName() {
            return "Actual";
        }

        @Override
        public Object getValue(String role) {
            switch (role) {
                case "actual": {
                    return loadProfile;
                }
                case "databaseValue": {
                    if (loadProfile == null) {
                        throw new IllegalArgumentException("Role '" + role + "' is not supported by identifier of type " + getTypeName());
                    } else {
                        return loadProfile.getId();
                    }
                }
                default: {
                    throw new IllegalArgumentException("Role '" + role + "' is not supported by identifier of type " + getTypeName());
                }
            }
        }
    }

}