/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocol.exceptions;

import com.energyict.mdc.upl.nls.MessageSeed;
import com.energyict.mdc.upl.nls.NlsService;

import java.text.MessageFormat;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * @author Stijn Vanhoorelbeke
 * @since 29.09.17 - 17:21
 */
public abstract class ProtocolRuntimeException extends RuntimeException {

    private MessageSeed messageSeed;
    private Object[] messageArguments;
    private Optional<NlsService> nlsService = Optional.empty();
    private Throwable originalCause = null;

    public ProtocolRuntimeException(MessageSeed messageSeed, Object... messageArguments) {
        super(defaultFormattedMessage(messageSeed, messageArguments));
        this.messageSeed = messageSeed;
        this.messageArguments = messageArguments;
    }

    public ProtocolRuntimeException(Throwable cause, MessageSeed messageSeed) {
        super(defaultFormattedMessage(messageSeed, new Object[]{cause.getMessage()}), cause);
        this.messageSeed = messageSeed;
        this.messageArguments = new Object[0];
        originalCause = cause;
    }

    public ProtocolRuntimeException(Throwable cause, MessageSeed messageSeed, Object... messageArguments) {
        super(defaultFormattedMessage(messageSeed, messageArguments), cause);
        this.messageSeed = messageSeed;
        this.messageArguments = messageArguments;
        originalCause = cause;
    }

    public ProtocolRuntimeException(Throwable cause) {
        super(cause.getMessage());
        originalCause = cause;
    }

    private static String defaultFormattedMessage(MessageSeed messageSeed, Object[] messageArguments) {
        return MessageFormat.format(messageSeed.getDefaultFormat(), messageArguments);
    }

    public MessageSeed getMessageSeed() {
        return this.messageSeed;
    }

    public Object[] getMessageArguments() {
        return (messageArguments.length == 0 && getCause() != null) // If no message arguments, then use the message of the cause as argument
                ? new Object[]{getCause().getMessage()}
                : messageArguments;
    }

    public void injectNlsService(NlsService nlsService) {
        this.nlsService = Optional.of(nlsService);
        if (getCause() instanceof ProtocolRuntimeException) {
            ((ProtocolRuntimeException) getCause()).injectNlsService(nlsService);
        }
    }

    @Override
    public String getMessage() {
        try {
        if (this.messageSeed != null ) {
            // the developer sent a message-seed
            return nlsService.isPresent()
                    ? nlsService.get().getThesaurus(getMessageSeed().getModule()).getFormat(getMessageSeed()).format(getMessageArguments())
                    : super.getMessage();
        }
        // no message seed, maybe some original cause?
        if (originalCause != null) {

            return originalCause.fillInStackTrace().getMessage();
        }
        // nothing :( so just fallback to n/a
        } catch (Exception ex) {
            Logger.getAnonymousLogger().warning(ex.getMessage());
        }
        return "n/a";
    }

    @Override
    public String getLocalizedMessage() {
        return getMessage();
    }
}