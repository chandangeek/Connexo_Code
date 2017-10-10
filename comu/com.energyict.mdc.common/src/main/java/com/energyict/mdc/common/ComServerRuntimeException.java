/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.util.exception.MessageSeed;

import java.text.MessageFormat;
import java.util.Optional;

/**
 * Serves as the root for all exceptions that we want to deal with at runtime.
 * Supports internationalization/localization (I18N) of messages and arguments
 * even after serialization. ComServerRuntimeExceptions can therefore
 * easily be stored in whatever persistent store and then resurrected
 * in a different I18N environment and translated to that new environment.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-03-26 (15:46)
 */
public abstract class ComServerRuntimeException extends ComServerExecutionException {

    private Object[] messageArguments;
    private MessageSeed messageSeed;

    private Optional<NlsService> nlsService = Optional.empty();

    /**
     * Constructs a new ComServerRuntimeException identified by the {@link MessageSeed}.
     *
     * @param messageSeed The MessageSeed
     * @param messageArguments A sequence of values for the arguments of the human readable description
     * that is associated with the MessageSeed
     */
    public ComServerRuntimeException(MessageSeed messageSeed, Object... messageArguments) {
        super(defaultFormattedMessage(messageSeed, messageArguments));
        this.messageSeed = messageSeed;
        this.messageArguments = messageArguments;
    }

    /**
     * Constructs a new ComServerRuntimeException identified by the {@link MessageSeed}.
     *
     * @param cause The actual cause of the exceptional situation
     * @param messageSeed The MessageSeed
     * that is associated with the MessageSeed
     */
    public ComServerRuntimeException(Throwable cause, MessageSeed messageSeed) {
        super(defaultFormattedMessage(messageSeed, new Object[]{cause.getMessage()}), cause);
        this.messageSeed = messageSeed;
        this.messageArguments = new Object[0];
    }

    /**
     * Constructs a new ComServerRuntimeException identified by the {@link MessageSeed}.
     *
     * @param cause The actual cause of the exceptional situation
     * @param messageSeed The MessageSeed
     * @param messageArguments A sequence of values for the arguments of the human readable description
     * that is associated with the MessageSeed
     */
    public ComServerRuntimeException(Throwable cause, MessageSeed messageSeed, Object... messageArguments) {
        super(defaultFormattedMessage(messageSeed, messageArguments), cause);
        this.messageSeed = messageSeed;
        this.messageArguments = messageArguments;
    }

    private static String defaultFormattedMessage(MessageSeed message, Object[] messageArguments) {
        return MessageFormat.format(message.getDefaultFormat(), messageArguments);
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
        if (getCause() instanceof ComServerRuntimeException) {
            ((ComServerRuntimeException) getCause()).injectNlsService(nlsService);
        }
    }

    @Override
    public String getMessage() {
        return nlsService.isPresent()
                ? nlsService.get().getThesaurus(getMessageSeed().getModule(), Layer.DOMAIN).getFormat(getMessageSeed()).format(getMessageArguments())
                : super.getMessage();
    }

    @Override
    public String getLocalizedMessage() {
        return nlsService.isPresent()
                ? nlsService.get().getThesaurus(getMessageSeed().getModule(), Layer.DOMAIN).getFormat(getMessageSeed()).format(getMessageArguments())
                : super.getLocalizedMessage();
    }
}