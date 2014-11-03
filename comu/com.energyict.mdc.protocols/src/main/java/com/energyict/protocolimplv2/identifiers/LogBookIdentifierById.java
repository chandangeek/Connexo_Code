package com.energyict.protocolimplv2.identifiers;

import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.NotFoundException;
import com.energyict.mdc.protocol.api.device.BaseLogBook;
import com.energyict.mdc.protocol.api.device.LogBookFactory;
import com.energyict.mdc.protocol.api.device.data.identifiers.LogBookIdentifier;
import com.energyict.mdc.io.CommunicationException;

import com.energyict.protocols.mdc.services.impl.MessageSeeds;

import java.util.List;

/**
 * Provides an implementation for the {@link LogBookIdentifier} interface
 * that uses an {@link com.energyict.mdc.protocol.api.device.BaseLogBook}'s database ID to uniquely identify it.
 *
 * Copyrights EnergyICT
 * Date: 13/05/13
 * Time: 13:16
 */
public class LogBookIdentifierById implements LogBookIdentifier {

    private final int logBookId;

    public LogBookIdentifierById(int logBookId) {
        super();
        this.logBookId = logBookId;
    }

    @Override
    public BaseLogBook getLogBook() {
        BaseLogBook logBook = this.getLogBookFactory().findLogBook(this.logBookId);
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
    public int getLogBookId() {
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
    public int hashCode () {
        return logBookId;
    }

    @Override
    public String toString() {
        return String.valueOf(this.logBookId);
    }

    private LogBookFactory getLogBookFactory() {
        List<LogBookFactory> factories = Environment.DEFAULT.get().getApplicationContext().getModulesImplementing(LogBookFactory.class);
        if (factories.isEmpty()) {
            throw new CommunicationException(MessageSeeds.MISSING_MODULE, LogBookFactory.class);
        }
        else {
            return factories.get(0);
        }
    }

}
