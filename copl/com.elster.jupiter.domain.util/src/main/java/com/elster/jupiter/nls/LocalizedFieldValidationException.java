/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.nls;

import com.elster.jupiter.util.exception.MessageSeed;

/**
 * LocalizedException that can be linked to a specific field of an Impl
 */
public class LocalizedFieldValidationException extends RuntimeException {

	private static final long serialVersionUID = 1L;
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

    /**
     * Creates a LocalizedFieldValidationException based on this exception,
     * but prepends the provided nodeName to the existing javaFieldName.
     * This method is practical in case a LocalizedFieldValidationException
     * needs to be caught and rethrown whilst adding information to
     * the nodePath / root property that caused the violation
     *
     * @param nodeName the path leading up to the property causing the violation
     * @return new LocalizedFieldValidationException with the extended nodePath
     */
    public LocalizedFieldValidationException fromSubField(String nodeName) {
        return new LocalizedFieldValidationException(this.messageSeed, nodeName+"."+this.javaFieldName, this.args);
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