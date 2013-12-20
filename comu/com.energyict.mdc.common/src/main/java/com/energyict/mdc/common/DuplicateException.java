package com.energyict.mdc.common;

/**
 * Instances of <CODE>DuplicateException</CODE> are thrown when
 * the requested action would violate the required
 * uniquenes of certain fields
 *
 * @author Karel
 */
public class DuplicateException extends BusinessException {

    /**
     * Creates new <code>DuplicateException</code> without detail message.
     */
    public DuplicateException() {
    }


    /**
     * Constructs an <code>DuplicateException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public DuplicateException(String msg) {
        super(msg);
    }

    /**
     * Creates a new DuplicateException
     *
     * @param messageId      message pattern key
     * @param defaultPattern default pattern
     * @param arg1           pattern first argument
     */
    public DuplicateException(String messageId, String defaultPattern, Object arg1) {
        super(messageId, defaultPattern, arg1);
    }

    /**
     * Creates a new DuplicateException
     *
     * @param messageId      message pattern key
     * @param defaultPattern default pattern
     * @param arg1           pattern first argument
     * @param arg2           pattern second argument
     */
    public DuplicateException(String messageId, String defaultPattern,
                              Object arg1, Object arg2) {
        super(messageId, defaultPattern, arg1, arg2);
    }

    /**
     * Creates a new DuplicateException
     *
     * @param messageId      message pattern key
     * @param defaultPattern default pattern
     * @param arg1           pattern first argument
     * @param arg2           pattern second argument
     * @param arg3           pattern third argument
     */
    public DuplicateException(String messageId, String defaultPattern,
                              Object arg1, Object arg2, Object arg3) {
        super(messageId, defaultPattern, arg1, arg2, arg3);
    }

    /**
     * Creates a new DuplicateException
     *
     * @param messageId      message pattern key
     * @param defaultPattern default pattern
     * @param arg1           pattern first argument
     * @param arg2           pattern second argument
     * @param arg3           pattern third argument
     * @param arg4           pattern fourth argument
     */
    public DuplicateException(String messageId, String defaultPattern,
                              Object arg1, Object arg2, Object arg3, Object arg4) {
        super(messageId, defaultPattern, arg1, arg2, arg3, arg4);
    }
}


