package com.energyict.mdc.common;

import java.text.MessageFormat;
import java.util.Arrays;

/**
 * BusinessException is the parent of all application
 * defined exceptions. It or its subclassses are thrown
 * when the requested action violates the implemented business rules
 *
 * @author Karel
 */
public class BusinessException extends java.lang.Exception {

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
        super(format(defaultPattern, arguments));
        this.messageId = messageId;
        this.arguments = Arrays.copyOf(arguments, arguments.length);
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
        super(format(defaultPattern, arguments), ex);
        this.messageId = messageId;
        this.arguments = Arrays.copyOf(arguments, arguments.length);
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
            return format(getPattern(messageId), arguments);
        }
    }

    /**
     * Returns the message pattern for the given key
     *
     * @param messageId message key
     * @return localized message pattern
     */
    public String getPattern(String messageId) {
        return messageId;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getErrorCode() {
        if (messageId != null) {
            return this.messageId;
        }
        else {
            return "EIS-UNKNOWN";
        }
    }

    public String getLocalizedErrorCodeMessage() {
        return getErrorCode() + ": " + getLocalizedMessage();
    }

    private static String format(String pattern, Object[] arguments) {
        // Since MessageFormat.format() interprets a single quote (')
        // as the start of a quoted string, a pattern string like
        // "It's recommended to change ... within {0,number{ day(s)"
        // isn't processed as expected: the {0, number} part is seen as part
        // of a quoted (no to touch) string and is NOT replaced by the 1st argument
        // eg. The result of
        // 1) MessageFormat.format("It's recommended to change ... within {0,number} day(s)", 10)
        // 2) MessageFormat.format("It's recommended to change '{0} to {0,number}' within {0,number} day(s)", 10)
        // 3) MessageFormat.format("It''s recommended to change '{0} to {0,number}' within {0,number} day(s)", 10)
        // is
        // 1) Its recommended to change ... within {0,number} day(s)         -- No argument replacement done
        // 2) Its recommended to change 10 to 10 within {0,number} day(s)    -- Wrong argument replacement done
        // 3) It's recommended to change {0} to {0,number} within 10 day(s)  -- Correct/As expected
        // Therefor in the pattern, we first replace each single quote
        // by two single quotes (indicating that quote is NOT the start of a quoted string)
        return MessageFormat.format(pattern.replaceAll("'", "''"), arguments);
    }

}
