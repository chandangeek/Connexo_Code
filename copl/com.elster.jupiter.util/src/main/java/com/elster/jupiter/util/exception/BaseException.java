/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.exception;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Base exception class for all exceptions in the Jupiter project.
 * Implementing classes should not expose the ExceptionType argument of the constructor in their own constructor.
 * The class comes with a Map that allows clients to add contextual information associated with String keys.
 */
public abstract class BaseException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private final MessageSeed messageSeed;
    private final Map<String, Object> context = new HashMap<>();

    protected BaseException(MessageSeed messageSeed, Throwable cause) {
        super(cause);
        this.messageSeed = messageSeed;
    }

    protected BaseException(MessageSeed messageSeed) {
        super(messageSeed.getDefaultFormat());
        this.messageSeed = messageSeed;
    }

    protected BaseException(MessageSeed messageSeed, Throwable cause, Object... args) {
        super(MessageFormat.format(messageSeed.getDefaultFormat(), args), cause);
        this.messageSeed = messageSeed;
    }

    protected BaseException(MessageSeed messageSeed, Object... args) {
        super(MessageFormat.format(messageSeed.getDefaultFormat(), args));
        this.messageSeed = messageSeed;
    }

    /**
     * Add contextual information to this exception as a key - value pair.
     * The intention is for this method to be used in a builder style pattern :
     * <code>
     *     throw new SomeException(ioExceptionface 2 face?).set("fileName", file.getName()).set("user", getOsUser());
     * </code>
     *
     * @param key
     * @param value
     * @return this
     */
    public BaseException set(String key, Object value) {
        context.put(key, value);
        return this;
    }

    public Object get(String key) {
        return context.get(key);
    }

    /**
     * @return an unmodifiable Map containing all contextual properties.
     */
    public Map<String, Object> getContextProperties() {
        return Collections.unmodifiableMap(context);
    }

    public MessageSeed getMessageSeed() {
        return messageSeed;
    }
}
