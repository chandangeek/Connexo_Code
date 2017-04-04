package com.energyict.protocolimplv2.identifiers;

import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;

import com.energyict.obis.ObisCode;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Implementation of a {@link RegisterIdentifier} that uniquely identifies an Register based on the ObisCode
 * of the RegisterMapping or the device obiscode of the RegisterSpec.
 * <p/>
 * Copyrights EnergyICT
 * Date: 13/05/13
 * Time: 13:24
 */
@XmlRootElement
public class RegisterDataIdentifierByObisCodeAndDevice implements RegisterIdentifier {

    private final ObisCode registerObisCode;
    private final DeviceIdentifier deviceIdentifier;

    // For JSON serialization only
    @SuppressWarnings("unused")
    private RegisterDataIdentifierByObisCodeAndDevice() {
        this.registerObisCode = null;
        this.deviceIdentifier = new NullDeviceIdentifier();
    }

    public RegisterDataIdentifierByObisCodeAndDevice(ObisCode registerObisCode, DeviceIdentifier deviceIdentifier) {
        this.registerObisCode = registerObisCode;
        this.deviceIdentifier = deviceIdentifier;
    }

    @Override
    public String toString() {
        return "deviceIdentifier = " + this.deviceIdentifier + " and ObisCode = " + this.registerObisCode.toString();
    }

    @XmlAttribute
    public ObisCode getRegisterObisCode() {
        return registerObisCode;
    }

    @XmlAttribute
    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RegisterDataIdentifierByObisCodeAndDevice that = (RegisterDataIdentifierByObisCodeAndDevice) o;
        return Objects.equals(registerObisCode, that.registerObisCode) &&
                Objects.equals(deviceIdentifier, that.deviceIdentifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(registerObisCode, deviceIdentifier);
    }

    @Override
    public com.energyict.mdc.upl.meterdata.identifiers.Introspector forIntrospection() {
        return new Introspector();
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
                    return getRegisterObisCode();
                }
                default: {
                    throw new IllegalArgumentException("Role '" + role + "' is not supported by identifier of type " + getTypeName());
                }
            }
        }
    }

    private static class NullDeviceIdentifier implements DeviceIdentifier {
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
        public Set<String> getRoles() {
            return Collections.emptySet();
        }

        @Override
        public Object getValue(String role) {
            throw new IllegalArgumentException("Role '" + role + "' is not supported by identifier of type " + getTypeName());
        }

    }

}