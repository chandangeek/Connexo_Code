package com.energyict.mdc.device.data.impl.identifiers;

import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier;

import com.energyict.obis.ObisCode;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Implementation of a {@link com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier} that uniquely identifies a LoadProfile
 * based on the ObisCode of the LoadProfile(type) and the {@link DeviceIdentifier}.<br/>
 * <b>Note: </b> we assume that it is never possible that two LoadProfiles with the same ObisCode are configured on the Device.<br/>
 * <b>Note2: </b> if the B-field of the ObisCode is marked as a wildcard, then make sure the provided loadProfileObisCode also has the wildcard!
 * <p/>
 * Copyrights EnergyICT
 * Date: 13/05/13
 * Time: 13:30
 */
@XmlRootElement
public class LoadProfileIdentifierByObisCodeAndDevice implements LoadProfileIdentifier {

    private final ObisCode loadProfileObisCode;
    private final DeviceIdentifier deviceIdentifier;

    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    @SuppressWarnings("unused")
    public LoadProfileIdentifierByObisCodeAndDevice() {
        this.loadProfileObisCode = null;
        this.deviceIdentifier = null;
    }

    public LoadProfileIdentifierByObisCodeAndDevice(ObisCode loadProfileObisCode, DeviceIdentifier deviceIdentifier) {
        super();
        this.loadProfileObisCode = loadProfileObisCode;
        this.deviceIdentifier = deviceIdentifier;
    }

    @Override
    public ObisCode getProfileObisCode() {
        return loadProfileObisCode;
    }

    @XmlAttribute
    public ObisCode getLoadProfileObisCode() {
        return loadProfileObisCode;
    }

    @XmlAttribute
    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }

    @Override
    public com.energyict.mdc.upl.meterdata.identifiers.Introspector forIntrospection() {
        return new Introspector();
    }

    @Override
    public String toString() {
        return MessageFormat.format("load profile having OBIS code {0} on device with deviceIdentifier ''{1}''", loadProfileObisCode, deviceIdentifier);
    }

    private class Introspector implements com.energyict.mdc.upl.meterdata.identifiers.Introspector {
        @Override
        public String getTypeName() {
            return "DeviceIdentifierAndObisCode";
        }

        @Override
        public Set<String> getRoles() {
            return new HashSet<>(Arrays.asList("device", "obisCode"));
        }

        @Override
        public Object getValue(String role) {
            switch (role) {
                case "device": {
                    return getDeviceIdentifier();
                }
                case "obisCode": {
                    return getLoadProfileObisCode();
                }
                default: {
                    throw new IllegalArgumentException("Role '" + role + "' is not supported by identifier of type " + getTypeName());
                }
            }
        }
    }

}