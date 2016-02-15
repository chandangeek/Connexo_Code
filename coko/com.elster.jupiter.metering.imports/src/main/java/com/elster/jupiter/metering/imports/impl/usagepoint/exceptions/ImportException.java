package com.elster.jupiter.metering.imports.impl.usagepoint.exceptions;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.text.MessageFormat;

public abstract class ImportException extends RuntimeException {
    private Object[] args;
    private MessageSeed message;

    public ImportException(MessageSeed message, Object... args){
        this.message = message;
        this.args = args;
    }

    @Override
    public String getMessage() {
        String message = this.message.getDefaultFormat();
        return getFormattedMessage(message);
    }

    private String getFormattedMessage(String message) {
        if (this.args != null) {
            return MessageFormat.format(message, this.args);
        }
        return message;
    }

    public String getLocalizedMessage(Thesaurus thesaurus) {
        return thesaurus.getFormat(message).format(this.args);
    }

}