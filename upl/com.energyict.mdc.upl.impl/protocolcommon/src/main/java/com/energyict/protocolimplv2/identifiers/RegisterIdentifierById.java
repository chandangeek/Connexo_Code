package com.energyict.protocolimplv2.identifiers;

import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;

import com.energyict.obis.ObisCode;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Copyrights EnergyICT
 * Date: 29/11/13
 * Time: 9:51
 * Author: khe
 */
@XmlRootElement
public class RegisterIdentifierById implements RegisterIdentifier {

    private final int id;
    private final ObisCode registerObisCode;
    private final DeviceIdentifier deviceIdentifier;

    // For JSON serialization only or in unit tests
    @SuppressWarnings("unused")
    public RegisterIdentifierById() {
        this.id = 0;
        this.registerObisCode = null;
        this.deviceIdentifier = null;
    }

    public RegisterIdentifierById(int id, ObisCode registerObisCode, DeviceIdentifier deviceIdentifier) {
        this.id = id;
        this.registerObisCode = registerObisCode;
        this.deviceIdentifier = deviceIdentifier;
    }

    public RegisterIdentifierById(long id, ObisCode registerObisCode, DeviceIdentifier deviceIdentifier) {
        this((int) id, registerObisCode, deviceIdentifier);
    }

    @Override
    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }

    @XmlAttribute
    public int getId() {
        return id;
    }

    @XmlAttribute
    public ObisCode getRegisterObisCode() {
        return registerObisCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RegisterIdentifierById otherIdentifier = (RegisterIdentifierById) o;
        return (this.id == otherIdentifier.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.valueOf(this.id);
    }

    @Override
    public com.energyict.mdc.upl.meterdata.identifiers.Introspector forIntrospection() {
        return new Introspector();
    }

    private class Introspector implements com.energyict.mdc.upl.meterdata.identifiers.Introspector {
        @Override
        public String getTypeName() {
            return "DatabaseId";
        }

        @Override
        public Set<String> getRoles() {
            return new HashSet<>(Arrays.asList("databaseValue", "device", "obisCode"));
        }

        @Override
        public Object getValue(String role) {
            switch (role) {
                case "databaseValue": {
                    return getId();
                }
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

}