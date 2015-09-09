package com.energyict.mdc.device.data.impl.identifiers;

import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.device.data.LogBookService;
import com.energyict.mdc.device.data.exceptions.CanNotFindForIdentifier;
import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.LogBookIdentifier;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Provides an implementation for the {@link LogBookIdentifier} interface
 * that uses an {@link com.energyict.mdc.protocol.api.device.BaseLogBook}'s database ID to uniquely identify it.
 *
 * @author sva
 * @since 10/12/12 - 16:01
 */
@XmlRootElement
public final class LogBookIdentifierById implements LogBookIdentifier<LogBook> {

    private final long logBookId;
    private final LogBookService logBookService;

    public LogBookIdentifierById(long logBookId, LogBookService logBookService) {
        super();
        this.logBookId = logBookId;
        this.logBookService = logBookService;
    }

    @Override
    public LogBook getLogBook() {
        return this.logBookService.findById(this.logBookId).orElseThrow(() -> CanNotFindForIdentifier.logBook(this, MessageSeeds.CAN_NOT_FIND_FOR_LOGBOOK_IDENTIFIER));
    }

    @Override
    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    @Override
    public void setXmlType(String ignore) {

    }

    @Override
    public DeviceIdentifier<?> getDeviceIdentifier() {
        return new DeviceIdentifierByLogBook(this);
    }

    /**
     * Getter for the Id of the {@link com.energyict.mdc.protocol.api.device.BaseLogBook}
     *
     * @return the Id
     */
    public long getLogBookId() {
        return logBookId;
    }

    /**
     * Check if the given {@link Object} is equal to this {@link LogBookIdentifierById}. <BR>
     * WARNING: if comparing with an {@link LogBookIdentifier} of another type (not of type {@link LogBookIdentifierById}),
     * this check will always return false, regardless of the fact they can both point to the same {@link com.energyict.mdc.protocol.api.device.BaseLogBook}!
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
        return String.valueOf(this.logBookId);
    }

}