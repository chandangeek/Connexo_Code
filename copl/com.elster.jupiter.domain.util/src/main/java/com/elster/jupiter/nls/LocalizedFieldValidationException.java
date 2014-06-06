package com.elster.jupiter.nls;

import com.elster.jupiter.util.exception.MessageSeed;

/**
 * LocalizedException that can be linked to a specific field of an Impl
 */
public class LocalizedFieldValidationException extends RuntimeException{
    private final MessageSeed messageSeed;
    private final String javaFieldName;
    private final Object[] args;


    public LocalizedFieldValidationException(MessageSeed messageSeed, String javaFieldName) {
        this(messageSeed, javaFieldName, new Object[0]);
    }

    public LocalizedFieldValidationException(MessageSeed messageSeed, String javaFieldName, Object... args) {
        this.messageSeed = messageSeed;
        this.javaFieldName = javaFieldName;
        this.args = args;
    }

    public String getViolatingProperty(){
        return javaFieldName;
    }

    public MessageSeed getMessageSeed() {
        return messageSeed;
    }

    public Object[] getArgs() {
        return args;
    }
}
