package com.energyict.protocolimplv2.identifiers;

import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.NotFoundException;
import com.energyict.mdc.protocol.api.device.LogBook;
import com.energyict.mdc.protocol.api.device.LogBookFactory;
import com.energyict.mdc.protocol.api.device.data.identifiers.LogBookIdentifier;
import com.energyict.mdc.protocol.api.exceptions.CommunicationException;

import java.util.List;

/**
 * Provides an implementation for the {@link LogBookIdentifier} interface
 * that uses an {@link LogBook}'s database ID to uniquely identify it.
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
    public LogBook getLogBook() {
        LogBook logBook = this.getLogBookFactory().findLogBook(this.logBookId);
        if (logBook == null) {
            throw new NotFoundException("LogBook with id " + this.logBookId + " not found");
        } else {
            return logBook;
        }
    }

    /**
     * Getter for the Id of the {@link LogBook}
     *
     * @return the Id
     */
    public int getLogBookId() {
        return logBookId;
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
        List<LogBookFactory> factories = Environment.DEFAULT.get().getApplicationContext().getModulesImplementing(LogBookFactory.class);
        if (factories.isEmpty()) {
            throw CommunicationException.missingModuleException(LogBookFactory.class);
        }
        else {
            return factories.get(0);
        }
    }

}
