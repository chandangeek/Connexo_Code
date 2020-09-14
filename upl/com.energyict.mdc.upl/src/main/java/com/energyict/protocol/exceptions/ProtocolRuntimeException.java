/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocol.exceptions;

import com.energyict.mdc.upl.nls.MessageSeed;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.nls.Thesaurus;

import java.text.MessageFormat;
import java.util.Optional;

/**
 * @author Stijn Vanhoorelbeke
 * @since 29.09.17 - 17:21
 */
public abstract class ProtocolRuntimeException extends RuntimeException {

    private MessageSeed messageSeed;
    private Object[] messageArguments;
    private Optional<NlsService> nlsService = Optional.empty();

    public ProtocolRuntimeException(MessageSeed messageSeed, Object... messageArguments) {
        super(defaultFormattedMessage(messageSeed, messageArguments));
        this.messageSeed = messageSeed;
        this.messageArguments = messageArguments;
    }

    public ProtocolRuntimeException(Throwable cause, MessageSeed messageSeed) {
        super(defaultFormattedMessage(messageSeed, new Object[]{cause.getMessage()}), cause);
        this.messageSeed = messageSeed;
        this.messageArguments = new Object[0];
    }

    public ProtocolRuntimeException(Throwable cause, MessageSeed messageSeed, Object... messageArguments) {
        super(defaultFormattedMessage(messageSeed, messageArguments), cause);
        this.messageSeed = messageSeed;
        this.messageArguments = messageArguments;
    }

    public ProtocolRuntimeException(Throwable cause) {
        super(cause.getMessage());
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
        return nlsService.isPresent()
                ? nlsService.get().getThesaurus(getMessageSeed().getModule()).getFormat(getMessageSeed()).format(getMessageArguments())
                : super.getMessage();
    }

    @Override
    public String getLocalizedMessage() {
        if (nlsService.isPresent()) {
            String module            = getMessageSeed().getModule();
            Thesaurus thesaurus      = nlsService.get().getThesaurus(module);
            MessageSeed messageSeed  = getMessageSeed();
            if (module != null && thesaurus != null && messageSeed != null)
            {
                return thesaurus.getFormat(messageSeed).format(getMessageArguments());
            }
        }

        return super.getLocalizedMessage();
    }
}