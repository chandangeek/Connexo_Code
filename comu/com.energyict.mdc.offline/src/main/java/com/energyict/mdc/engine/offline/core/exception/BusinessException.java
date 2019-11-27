/*
 * BusinessException.java
 *
 * Created on 6 november 2001, 9:53
 */

package com.energyict.mdc.engine.offline.core.exception;

import com.energyict.mdc.engine.offline.core.TranslatorProvider;
import com.energyict.mdc.engine.offline.core.Utils;

/**
 * BusinessException is the parent of all application
 * defined exceptions. It or its subclassses are thrown
 * when the requested action violates the implemented business rules
 *
 * @author Karel
 */
public class BusinessException extends Exception {

    protected String messageId;
    protected Object[] arguments;

    /**
     * Creates new <code>BusinessException</code> without detail message.
     */
    public BusinessException() {
    }

    /**
     * Constructs an <code>BusinessException</code> with the specified detail message.
     *
     * @param msg the detail message.
     * @deprecated Use constructor with messageId, defaultPattern
     */
    @Deprecated
    public BusinessException(String msg) {
        super(msg);
    }

    /**
     * Constructs a new <CODE>BusinessException</CODE> with the specified cause
     *
     * @param ex underlying cause of the new <CODE>BusinessException</CODE>
     * @deprecated Use constructor with messageId, defaultPattern
     */
    @Deprecated
    public BusinessException(Throwable ex) {
        super(ex);
    }


    /**
     * Constructs a new <CODE>BusinessException</CODE>.
     *
     * @param messageId      localization key
     * @param defaultPattern default pattern
     * @param arguments      message arguments
     */
    public BusinessException(String messageId, String defaultPattern, Object[] arguments) {
        super(Utils.format(defaultPattern, arguments));
        this.messageId = messageId;
        this.arguments = arguments;
    }

    /**
     * Constructs a new <CODE>BusinessException</CODE>.
     *
     * @param messageId      localization key
     * @param defaultPattern default pattern
     * @param arguments      message arguments
     * @param ex             underlying cause of the new <CODE>BusinessException</CODE>
     */
    public BusinessException(String messageId, String defaultPattern, Object[] arguments, Throwable ex) {
        super(Utils.format(defaultPattern, arguments), ex);
        this.messageId = messageId;
        this.arguments = arguments;
    }

    /**
     * Constructs a new <CODE>BusinessException</CODE>.
     *
     * @param messageId      localization key
     * @param defaultPattern default pattern
     */
    public BusinessException(String messageId, String defaultPattern) {
        this(messageId, defaultPattern, new Object[0]);
    }

    /**
     * Constructs a new <CODE>BusinessException</CODE>.
     *
     * @param messageId      localization key
     * @param defaultPattern default pattern
     * @param arg1           first and only message argument
     */
    public BusinessException(String messageId, String defaultPattern, Object arg1) {
        this(messageId, defaultPattern, new Object[]{arg1});
    }

    /**
     * Constructs a new <CODE>BusinessException</CODE>.
     *
     * @param messageId      localization key
     * @param defaultPattern default pattern
     * @param arg1           first message argument
     * @param arg2           second message argument
     */
    public BusinessException(String messageId, String defaultPattern, Object arg1, Object arg2) {
        this(messageId, defaultPattern, new Object[]{arg1, arg2});
    }

    /**
     * Constructs a new <CODE>BusinessException</CODE>.
     *
     * @param messageId      localization key
     * @param defaultPattern default pattern
     * @param arg1           first message argument
     * @param arg2           seconde message argument
     * @param arg3           third message argument
     */
    public BusinessException(String messageId, String defaultPattern, Object arg1, Object arg2, Object arg3) {
        this(messageId, defaultPattern, new Object[]{arg1, arg2, arg3});
    }

    /**
     * Constructs a new <CODE>BusinessException</CODE>.
     *
     * @param messageId      localization key
     * @param defaultPattern default pattern
     * @param arg1           first message argument
     * @param arg2           seconde message argument
     * @param arg3           third message argument
     * @param arg4           fourth message argument
     */
    public BusinessException(String messageId, String defaultPattern, Object arg1, Object arg2, Object arg3, Object arg4) {
        this(messageId, defaultPattern, new Object[]{arg1, arg2, arg3, arg4});
    }

    /**
     * Returns a localized message
     *
     * @return a localized String
     */
    public String getLocalizedMessage() {
        if (messageId == null) {
            return super.getLocalizedMessage();
        } else {
            return Utils.format(getPattern(messageId), arguments);
        }
    }

    /**
     * Returns the message pattern for the given key
     *
     * @param messageId message key
     * @return localized message pattern
     */
    public String getPattern(String messageId) {
        return TranslatorProvider.instance.get().getTranslator().getErrorMsg(messageId);
    }

    public String getMessageId() {
        return messageId;
    }

    public String getErrorCode() {
        return (messageId != null ? TranslatorProvider.instance.get().getTranslator().getErrorCode(messageId) : "EIS-UNKNOWN");
    }

    public String getLocalizedErrorCodeMessage() {
        return getErrorCode() + ": " + getLocalizedMessage();
    }
}
