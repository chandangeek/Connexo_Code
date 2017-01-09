package com.energyict.protocol.exceptions;

import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.nls.MessageSeed;

/**
 * Models the exceptional situation that occurs when a connection
 * with a device could not be established.
 * There will always be a nested exception that
 * provides details of the failure.
 *
 * This exception should NOT be thrown by any protocol, it is framework related.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-06-15 (12:10)
 */
public class ConnectionException extends Exception {

    private final String thesaurusId;
    private final MessageSeed messageSeed;
    private final Object[] messageArgs;

    public ConnectionException(String thesaurusId, MessageSeed messageSeed, Throwable cause) {
        super(messageSeed.getDefaultFormat(), cause);
        this.thesaurusId = thesaurusId;
        this.messageSeed = messageSeed;
        this.messageArgs = new Object[] {};
    }

    public ConnectionException(String thesaurusId, MessageSeed messageSeed, Object... arguments) {
        super(messageSeed.getDefaultFormat());
        this.thesaurusId = thesaurusId;
        this.messageSeed = messageSeed;
        this.messageArgs = arguments;
    }

    public ConnectionException(String thesaurusId, MessageSeed messageSeed, Throwable cause, Object... arguments) {
        super(messageSeed.getDefaultFormat(), cause);
        this.thesaurusId = thesaurusId;
        this.messageSeed = messageSeed;
        this.messageArgs = arguments;
    }

    @Override
    public String getLocalizedMessage() {
        return Services
                .nlsService()
                .getThesaurus(this.thesaurusId)
                .getFormat(this.messageSeed)
                .format(this.messageArgs);
    }

}