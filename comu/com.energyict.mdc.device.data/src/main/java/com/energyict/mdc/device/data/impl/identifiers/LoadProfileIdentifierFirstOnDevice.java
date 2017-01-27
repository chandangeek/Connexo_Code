package com.energyict.mdc.device.data.impl.identifiers;

import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier;

import com.energyict.obis.ObisCode;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Provides an implementation for the {@link LoadProfileIdentifier}
 * that returns the first LoadProfile that is found
 * on a Device.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-07-02 (11:52)
 */
@XmlRootElement
public class LoadProfileIdentifierFirstOnDevice implements LoadProfileIdentifier {

    private final ObisCode profileObisCode;
    private DeviceIdentifier deviceIdentifier;

    /**
     * Constructor only to be used by JSON (de)marshalling.
     */
    @SuppressWarnings("unused")
    public LoadProfileIdentifierFirstOnDevice() {
        profileObisCode = null;
    }

    public LoadProfileIdentifierFirstOnDevice(DeviceIdentifier deviceIdentifier, ObisCode profileObisCode) {
        this.deviceIdentifier = deviceIdentifier;
        this.profileObisCode = profileObisCode;
    }

    @Override
    public ObisCode getProfileObisCode() {
        return profileObisCode;
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
        return MessageFormat.format("fist load profile on device with deviceIdentifier ''{0}''", deviceIdentifier);
    }

    private class Introspector implements com.energyict.mdc.upl.meterdata.identifiers.Introspector {
        @Override
        public String getTypeName() {
            return "FirstLoadProfileOnDevice";
        }

        @Override
        public Set<String> getRoles() {
            return new HashSet<>(Collections.singletonList("device"));
        }

        @Override
        public Object getValue(String role) {
            if ("device".equals(role)) {
                return getDeviceIdentifier();
            } else {
                throw new IllegalArgumentException("Role '" + role + "' is not supported by identifier of type " + getTypeName());
            }
        }
    }

}