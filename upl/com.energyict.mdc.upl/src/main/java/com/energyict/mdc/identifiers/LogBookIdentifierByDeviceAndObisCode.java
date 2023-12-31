/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.identifiers;

import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LogBookIdentifier;

import com.energyict.obis.ObisCode;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Provides an implementation for the {@link LogBookIdentifier} interface
 * that uses a device's {@link DeviceIdentifier identifier} and the {@link ObisCode}
 * of the logbook to identify it.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-11-05 (13:24)
 */
@XmlRootElement
public class LogBookIdentifierByDeviceAndObisCode implements LogBookIdentifier {

    private final DeviceIdentifier deviceIdentifier;
    private final ObisCode logBookObisCode;

    // For JSON serialization only or in unit tests
    public LogBookIdentifierByDeviceAndObisCode() {
        this.deviceIdentifier = null;
        this.logBookObisCode = null;
    }

    public LogBookIdentifierByDeviceAndObisCode(DeviceIdentifier deviceIdentifier, ObisCode logBookObisCode) {
        super();
        this.deviceIdentifier = deviceIdentifier;
        this.logBookObisCode = logBookObisCode;
    }

    @Override
    public com.energyict.mdc.upl.meterdata.identifiers.Introspector forIntrospection() {
        return new Introspector();
    }

    @Override
    public ObisCode getLogBookObisCode() {
        return logBookObisCode;
    }

    @XmlElements( {
            @XmlElement(type = DeviceIdentifierById.class),
            @XmlElement(type = DeviceIdentifierBySerialNumber.class),
            @XmlElement(type = DeviceIdentifierByMRID.class),
            @XmlElement(type = DeviceIdentifierForAlreadyKnownDevice.class),
            @XmlElement(type = DeviceIdentifierByDeviceName.class),
            @XmlElement(type = DeviceIdentifierByConnectionTypeAndProperty.class),
    })
    @Override
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
        LogBookIdentifierByDeviceAndObisCode that = (LogBookIdentifierByDeviceAndObisCode) o;
        return Objects.equals(deviceIdentifier, that.deviceIdentifier) &&
                Objects.equals(logBookObisCode, that.logBookObisCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.deviceIdentifier, this.logBookObisCode);
    }

    @Override
    public String toString() {
        return MessageFormat.format("logbook having OBIS code {0} on device with deviceIdentifier ''{1}''", logBookObisCode, deviceIdentifier);
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
                    return deviceIdentifier;
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