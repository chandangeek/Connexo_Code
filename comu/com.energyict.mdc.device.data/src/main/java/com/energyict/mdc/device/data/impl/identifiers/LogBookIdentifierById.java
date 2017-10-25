/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.identifiers;

import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LogBookIdentifier;

import com.energyict.obis.ObisCode;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Provides an implementation for the {@link LogBookIdentifier} interface
 * that uses an {@link com.energyict.mdc.upl.meterdata.LogBook}'s database ID to uniquely identify it.
 *
 * @author sva
 * @since 10/12/12 - 16:01
 */
@XmlRootElement
public final class LogBookIdentifierById implements LogBookIdentifier {

    private final long logBookId;
    private final ObisCode obisCode;
    private final DeviceIdentifier deviceIdentifier;

    // For JSON serialization only or in unit tests
    public LogBookIdentifierById() {
        this.logBookId = 0;
        this.obisCode = null;
        this.deviceIdentifier = null;
    }

    public LogBookIdentifierById(long logBookId, ObisCode obisCode, DeviceIdentifier deviceIdentifier) {
        super();
        this.logBookId = logBookId;
        this.obisCode = obisCode;
        this.deviceIdentifier = deviceIdentifier;
    }

    @Override
    public ObisCode getLogBookObisCode() {
        return obisCode;
    }

    @Override
    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }

    @Override
    public com.energyict.mdc.upl.meterdata.identifiers.Introspector forIntrospection() {
        return new Introspector();
    }

    /**
     * Getter for the Id of the {@link com.energyict.mdc.upl.meterdata.LogBook}
     *
     * @return the Id
     */
    public long getLogBookId() {
        return logBookId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LogBookIdentifierById that = (LogBookIdentifierById) o;
        return logBookId == that.logBookId;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(this.logBookId);
    }

    @Override
    public String toString() {
        return "logbook having id " + this.logBookId;
    }

    private class Introspector implements com.energyict.mdc.upl.meterdata.identifiers.Introspector {
        @Override
        public String getTypeName() {
            return "DatabaseId";
        }

        @Override
        public Set<String> getRoles() {
            return new HashSet<>(Collections.singletonList("databaseValue"));
        }

        @Override
        public Object getValue(String role) {
            if ("databaseValue".equals(role)) {
                return getLogBookId();
            } else {
                throw new IllegalArgumentException("Role '" + role + "' is not supported by identifier of type " + getTypeName());
            }
        }
    }

}