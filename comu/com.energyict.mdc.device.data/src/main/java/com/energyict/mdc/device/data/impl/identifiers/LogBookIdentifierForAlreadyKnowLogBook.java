/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.identifiers;

import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LogBookIdentifier;

import com.energyict.obis.ObisCode;

import javax.xml.bind.annotation.XmlRootElement;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a LogBookIdentifier for a LogBook that is already known
 * at construction time and should there not be fetched from database anymore.
 */
@XmlRootElement
public class LogBookIdentifierForAlreadyKnowLogBook implements LogBookIdentifier {

    private final LogBook logBook;
    private final DeviceIdentifier deviceIdentifier;

    public LogBookIdentifierForAlreadyKnowLogBook(LogBook logBook, DeviceIdentifier deviceIdentifier) {
        this.logBook = logBook;
        this.deviceIdentifier = deviceIdentifier;
    }

    @Override
    public ObisCode getLogBookObisCode() {
        return logBook.getDeviceObisCode();
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
    public String toString() {
        return MessageFormat.format(
                "logbook with name ''{0}'' on device with name ''{1}''",
                logBook.getLogBookType().getName(),
                logBook.getDevice().getName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LogBookIdentifierForAlreadyKnowLogBook that = (LogBookIdentifierForAlreadyKnowLogBook) o;
        return Objects.equals(logBook, that.logBook);
    }

    @Override
    public int hashCode() {
        return this.logBook.hashCode();
    }

    private class Introspector implements com.energyict.mdc.upl.meterdata.identifiers.Introspector {
        @Override
        public String getTypeName() {
            return "Actual";
        }

        @Override
        public Set<String> getRoles() {
            return new HashSet<>(Arrays.asList("actual", "databaseValue"));
        }

        @Override
        public Object getValue(String role) {
            switch (role) {
                case "actual": {
                    return logBook;
                }
                case "databaseValue": {
                    return logBook.getId();
                }
                default: {
                    throw new IllegalArgumentException("Role '" + role + "' is not supported by identifier of type " + getTypeName());
                }
            }
        }
    }

}
