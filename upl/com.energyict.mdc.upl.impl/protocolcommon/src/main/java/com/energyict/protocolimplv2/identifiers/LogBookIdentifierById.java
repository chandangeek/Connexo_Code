package com.energyict.protocolimplv2.identifiers;

import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LogBookIdentifier;
import com.energyict.obis.ObisCode;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Provides an implementation for the {@link LogBookIdentifier} interface
 * that uses an LogBook's database ID to uniquely identify it.
 * <p>
 * Copyrights EnergyICT
 * Date: 13/05/13
 * Time: 13:16
 */
@XmlRootElement
public class LogBookIdentifierById implements LogBookIdentifier {

    private final int logBookId;
    private final ObisCode logBookObisCode;
    private final DeviceIdentifier deviceIdentifier;

    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    private LogBookIdentifierById() {
        this.logBookId = -1;
        this.logBookObisCode = null;
        this.deviceIdentifier = null;
    }

    public LogBookIdentifierById(int logBookId, ObisCode logBookDeviceObisCode, DeviceIdentifier deviceIdentifier) {
        super();
        this.logBookId = logBookId;
        this.logBookObisCode = logBookDeviceObisCode;
        this.deviceIdentifier = deviceIdentifier;
    }

    @XmlAttribute
    public int getLogBookId() {
        return logBookId;
    }

    @XmlAttribute
    public ObisCode getLogBookObisCode() {
        return logBookObisCode;
    }

    @Override
    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
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
        LogBookIdentifierById otherIdentifier = (LogBookIdentifierById) o;
        return (this.logBookId == otherIdentifier.logBookId);
    }

    @Override
    public int hashCode() {
        return logBookId;
    }

    @Override
    public String toString() {
        return String.valueOf(this.logBookId);
    }

    private class Introspector implements com.energyict.mdc.upl.meterdata.identifiers.Introspector {
        @Override
        public String getTypeName() {
            return "DatabaseId";
        }

        @Override
        public Set<String> getRoles() {
            return new HashSet<>(Collections.singletonList("databaserValue"));
        }

        @Override
        public Object getValue(String role) {
            if ("databaseValue".equals(role)) {
                return logBookId;
            } else {
                throw new IllegalArgumentException("Role '" + role + "' is not supported by identifier of type " + getTypeName());
            }
        }

    }

}