package com.energyict.mdc.device.data.impl.identifiers;

import com.energyict.mdc.upl.meterdata.identifiers.LogBookIdentifier;

import com.energyict.obis.ObisCode;

import javax.xml.bind.annotation.XmlRootElement;

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

    public LogBookIdentifierById(long logBookId, ObisCode obisCode) {
        super();
        this.logBookId = logBookId;
        this.obisCode = obisCode;
    }

    @Override
    public ObisCode getLogBookObisCode() {
        return obisCode;
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

    /**
     * Check if the given {@link Object} is equal to this {@link LogBookIdentifierById}. <BR>
     * WARNING: if comparing with an {@link LogBookIdentifier} of another type (not of type {@link LogBookIdentifierById}),
     * this check will always return false, regardless of the fact they can both point to the same {@link com.energyict.mdc.upl.meterdata.LogBook}!
     */
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
        return Long.valueOf(this.logBookId).hashCode();
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
        public Object getValue(String role) {
            if ("databaseValue".equals(role)) {
                return getLogBookId();
            } else {
                throw new IllegalArgumentException("Role '" + role + "' is not supported by identifier of type " + getTypeName());
            }
        }
    }

}