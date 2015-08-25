package com.energyict.mdc.io;

import com.energyict.mdc.common.ComServerRuntimeException;

import com.elster.jupiter.util.exception.MessageSeed;

import java.io.IOException;

/**
 * Models the exceptional situation that occurs when underlying
 * communication mechanisms report an IOException.
 * The design is that these will be caught by by the executing
 * communication server thread and dumped as a ComTaskExecutionJournalEntry
 * in the logging of the related ComTask.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-06-15 (10:21)
 */
public class CommunicationException extends ComServerRuntimeException {

    public CommunicationException(MessageSeed messageSeed, IOException cause) {
        super(cause, messageSeed);
    }

    public CommunicationException(MessageSeed messageSeed, NumberFormatException cause, String parameterName, String value) {
        super(cause, messageSeed, parameterName, value);
    }

    public CommunicationException (MessageSeed messageSeed, Object... messageArguments) {
        super(messageSeed, messageArguments);
    }

    protected CommunicationException(MessageSeed messageSeed, Exception cause) {
        super(cause, messageSeed, cause.getMessage());
    }

}