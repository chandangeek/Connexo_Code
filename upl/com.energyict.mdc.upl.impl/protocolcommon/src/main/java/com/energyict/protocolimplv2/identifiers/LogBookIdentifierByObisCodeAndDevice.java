package com.energyict.protocolimplv2.identifiers;

import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LogBookIdentifier;

import com.energyict.obis.ObisCode;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Provides an implementation for the {@link LogBookIdentifier} interface
 * that uses a device's {@link DeviceIdentifier} and the {@link com.energyict.obis.ObisCode} of the logbook to identify it
 * <p/>
 * Copyrights EnergyICT
 * Date: 13/05/13
 * Time: 16:12
 */
@XmlRootElement
public class LogBookIdentifierByObisCodeAndDevice implements LogBookIdentifier {

    private DeviceIdentifier deviceIdentifier;
    private ObisCode logBookObisCode;

    // For JSON serialization only or in unit tests
    public LogBookIdentifierByObisCodeAndDevice() {
        super();
    }

    public LogBookIdentifierByObisCodeAndDevice(DeviceIdentifier deviceIdentifier, ObisCode logBookObisCode) {
        this();
        this.deviceIdentifier = deviceIdentifier;
        this.logBookObisCode = logBookObisCode;
    }

    @XmlAttribute
    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }

    @XmlAttribute
    public ObisCode getLogBookObisCode() {
        return logBookObisCode;
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
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LogBookIdentifierByObisCodeAndDevice otherIdentifier = (LogBookIdentifierByObisCodeAndDevice) o;
        return this.deviceIdentifier.equals(otherIdentifier.getDeviceIdentifier())
            && logBookObisCode.equals(otherIdentifier.getLogBookObisCode());
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceIdentifier, logBookObisCode);
    }

    @Override
    public String toString() {
        return "Identifier for logbook with obiscode '" + logBookObisCode.toString() + "' on " + deviceIdentifier.toString();
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
                    return getLogBookObisCode();
                }
                default: {
                    throw new IllegalArgumentException("Role '" + role + "' is not supported by identifier of type " + getTypeName());
                }
            }
        }
    }

}