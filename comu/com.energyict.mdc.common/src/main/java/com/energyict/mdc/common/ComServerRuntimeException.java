package com.energyict.mdc.common;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

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

    /**
     * Constructs a new ComServerRuntimeException identified by the {@link MessageSeed}.
     *
     * @param messageSeed The MessageSeed
     * @param messageArguments A sequence of values for the arguments of the human readable description
     *        that is associated with the MessageSeed
     */
    public ComServerRuntimeException (MessageSeed messageSeed, Object... messageArguments) {
        super();
        this.messageSeed = messageSeed;
        this.messageArguments = messageArguments;
    }

    /**
     * Constructs a new ComServerRuntimeException identified by the {@link MessageSeed}.
     * <p>
     * The error message of the cause exception (cause) is added to the new error message.
     * Both error messages are split by {@code ComServerRuntimeException.DEFAULT_CAUSED_BY_SEPARATOR_VALUE},
     * but this value can be overridden by defining a
     * value for the key {@code ComServerRuntimeException.CAUSED_BY_SEPARATOR}
     * in one of your resource bundles.
     *
     * @param cause The actual cause of the exceptional situation
     * @param messageSeed The MessageSeed
     * @param messageArguments A sequence of values for the arguments of the human readable description
     *                         that is associated with the MessageSeed
     */
    public ComServerRuntimeException (Throwable cause, MessageSeed messageSeed, Object... messageArguments) {
        super(cause);
        this.messageSeed = messageSeed;
        this.messageArguments = messageArguments;
    }

    public MessageSeed getMessageSeed() {
        return this.messageSeed;
    }

    public Object[] getMessageArguments() {
        return messageArguments;
    }

    public String translated (NlsService nlsService) {
        Thesaurus thesaurus = nlsService.getThesaurus(this.messageSeed.getModule(), Layer.DOMAIN);
        return thesaurus.getFormat(messageSeed).format(this.messageArguments);
    }

}