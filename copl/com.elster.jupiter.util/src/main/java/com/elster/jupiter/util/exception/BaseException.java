package com.elster.jupiter.util.exception;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Base exception class for all exceptions in the Jupiter project.
 * Implementing classes should not expose the ExceptionType argument of the constructor in their own constructor.
 * The class comes with a Map that allows clients to add contextual information associated with String keys.
 */
public abstract class BaseException extends RuntimeException {

    private final ExceptionType type;
    private final Map<String, Object> context = new HashMap<>();

    protected BaseException(ExceptionType type, Throwable cause) {
        super(cause);
        this.type = type;
    }

    protected BaseException(ExceptionType type, String message) {
        super(message);
        this.type = type;
    }

    protected BaseException(ExceptionType type, String message, Throwable cause) {
        super(message, cause);
        this.type = type;
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

    /**
     * @return an unmodifiable Map containing all contextual properties.
     */
    public Map<String, Object> getContextProperties() {
        return Collections.unmodifiableMap(context);
    }

}
