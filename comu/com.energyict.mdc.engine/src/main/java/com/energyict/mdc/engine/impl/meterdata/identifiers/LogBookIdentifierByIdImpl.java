package com.energyict.mdc.engine.impl.meterdata.identifiers;

import com.energyict.mdc.common.NotFoundException;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.protocol.api.device.BaseLogBook;
import com.energyict.mdc.protocol.api.device.data.identifiers.LogBookIdentifier;

/**
 * Provides an implementation for the {@link LogBookIdentifier} interface
 * that uses an {@link com.energyict.mdc.protocol.api.device.BaseLogBook}'s database ID to uniquely identify it.
 *
 * @author sva
 * @since 10/12/12 - 16:01
 */

public class LogBookIdentifierByIdImpl implements LogBookIdentifier {

    private final long logBookId;
    private final DeviceDataService deviceDataService;

    public LogBookIdentifierByIdImpl(long logBookId, DeviceDataService deviceDataService) {
        super();
        this.logBookId = logBookId;
        this.deviceDataService = deviceDataService;
    }

    @Override
    public BaseLogBook getLogBook() {
        LogBook logBook = this.deviceDataService.findLogBookById(this.logBookId);
        if (logBook == null) {
            throw new NotFoundException("LogBook with id " + this.logBookId + " not found");
        } else {
            return logBook;
        }
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
     * Check if the given {@link Object} is equal to this {@link LogBookIdentifierByIdImpl}. <BR>
     * WARNING: if comparing with an {@link LogBookIdentifier} of another type (not of type {@link LogBookIdentifierByIdImpl}),
     * this check will always return false, regardless of the fact they can both point to the same {@link com.energyict.mdc.protocol.api.device.BaseLogBook}!
     */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LogBookIdentifierByIdImpl otherIdentifier = (LogBookIdentifierByIdImpl) o;
        return (this.logBookId == otherIdentifier.logBookId);
    }

    @Override
    public int hashCode () {
        return Long.valueOf(this.logBookId).hashCode();
    }

    @Override
    public String toString() {
        return String.valueOf(this.logBookId);
    }

}