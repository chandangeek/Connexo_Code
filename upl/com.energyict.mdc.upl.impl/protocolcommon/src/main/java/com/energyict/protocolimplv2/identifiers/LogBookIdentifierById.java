package com.energyict.protocolimplv2.identifiers;

import com.energyict.mdc.upl.meterdata.identifiers.LogBookIdentifier;

import com.energyict.mdw.core.LogBook;
import com.energyict.mdw.core.LogBookFactory;
import com.energyict.mdw.core.LogBookFactoryProvider;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.exceptions.identifier.NotFoundException;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Provides an implementation for the {@link LogBookIdentifier} interface
 * that uses an {@link com.energyict.mdw.core.LogBook}'s database ID to uniquely identify it.
 *
 * Copyrights EnergyICT
 * Date: 13/05/13
 * Time: 13:16
 */
@XmlRootElement
public class LogBookIdentifierById implements LogBookIdentifier {

    private final int logBookId;
    private final ObisCode logBookObisCode;

    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    private LogBookIdentifierById() {
        this.logBookId = -1;
        this.logBookObisCode = null;
    }

    public LogBookIdentifierById(int logBookId, ObisCode logBookDeviceObisCode) {
        super();
        this.logBookId = logBookId;
        this.logBookObisCode = logBookDeviceObisCode;
    }

    @Override
    public LogBook getLogBook() {
        LogBook logBook = getLogBookFactory().find(this.logBookId);
        if (logBook == null) {
            throw NotFoundException.notFound(LogBook.class, this.toString());
        } else {
            return logBook;
        }
    }

    /**
     * Getter for the Id of the {@link LogBook}
     *
     * @return the Id
     */
    @XmlAttribute
    public int getLogBookId() {
        return logBookId;
    }

    @XmlAttribute
    public ObisCode getLogBookObisCode() {
        return logBookObisCode;
    }

    @Override
    public com.energyict.mdc.upl.meterdata.identifiers.Introspector forIntrospection() {
        return new Introspector();
    }

    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    public void setXmlType(String ignore) {
        // For xml unmarshalling purposes only
    }

    /**
     * Check if the given {@link Object} is equal to this {@link LogBookIdentifierById}. <BR>
     * WARNING: if comparing with an {@link LogBookIdentifier} of another type (not of type {@link LogBookIdentifierById}),
     * this check will always return false, regardless of the fact they can both point to the same {@link LogBook}!
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
    public int hashCode () {
        return logBookId;
    }

    @Override
    public String toString() {
        return String.valueOf(this.logBookId);
    }

    private LogBookFactory getLogBookFactory() {
        return LogBookFactoryProvider.instance.get().getLogBookFactory();
    }

    private class Introspector implements com.energyict.mdc.upl.meterdata.identifiers.Introspector {
        @Override
        public String getTypeName() {
            return "DatabaseId";
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