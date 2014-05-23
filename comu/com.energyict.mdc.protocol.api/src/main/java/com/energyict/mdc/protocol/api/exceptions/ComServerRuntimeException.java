package com.energyict.mdc.protocol.api.exceptions;

import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.Translator;
import com.energyict.mdc.common.exceptions.ExceptionCode;

import java.text.MessageFormat;

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

    private String messageId;
    private Object[] messageArguments;
    private ExceptionCode exceptionCode;

    /**
     * Constructs a new ComServerRuntimeException identified by the {@link ExceptionCode}.
     *
     * @param code The ExceptionCode
     * @param messageArguments A sequence of values for the arguments of the human readable description
     *        that is associated with the ExceptionCode
     */
    public ComServerRuntimeException (ExceptionCode code, Object... messageArguments) {
        super();
        this.exceptionCode = code;
        this.messageId = code.toMessageResourceKey();
        this.messageArguments = messageArguments;
        assert numberOfParametersMatch(code.getExpectedNumberOfMessageArguments(), messageArguments) : "Wrong number of arguments for exception message";
    }

    /**
     * Returns the exception code that uniquely identifies the type of exception
     * @return the exception code
     */
    public ExceptionCode getExceptionCode() {
        return exceptionCode;
    }

    /**
     * Constructs a new ComServerRuntimeException identified by the {@link ExceptionCode}.
     * <p>
     * Enabling assertions at development time will detect the situation where
     * the number of actual parameters does not match the expected
     * number of parameters which is in fact a "not so harmful" coding error.
     * <p>
     * The error message of the cause exception (cause) is added to the new error message.
     * Both error messages are split by {@code ComServerRuntimeException.DEFAULT_CAUSED_BY_SEPARATOR_VALUE},
     * but this value can be overridden by defining a
     * value for the key {@code ComServerRuntimeException.CAUSED_BY_SEPARATOR}
     * in one of your resource bundles.
     *
     * @param cause The actual cause of the exceptional situation
     * @param code The ExceptionCode
     * @param messageArguments A sequence of values for the arguments of the human readable description
     *                         that is associated with the ExceptionCode
     */
    public ComServerRuntimeException (Throwable cause, ExceptionCode code, Object... messageArguments) {
        super(cause);
        this.messageId = code.toMessageResourceKey();
        this.messageArguments = messageArguments;
        assert numberOfParametersMatch(code.getExpectedNumberOfMessageArguments(), messageArguments) : "Wrong number of arguments for exception message";
    }

    public String getLocalizedMessage() {
        return defaultFormattedMessage(this.messageId, this.messageArguments);
    }

    private static String defaultFormattedMessage (String messageId, Object[] messageArguments) {
        String errorMsg = getTranslator().getErrorMsg(messageId);
        return MessageFormat.format(errorMsg.replaceAll("'", "''"), messageArguments);
    }

    private static Translator getTranslator() {
        Environment environment = Environment.DEFAULT.get();
        if (environment != null) {
            return environment;
        }
        else {
            return new Translator() {
                @Override
                public String getTranslation(String key) {
                    return key;
                }

                @Override
                public String getTranslation(String key, boolean flagError) {
                    return key;
                }

                @Override
                public String getErrorMsg(String key) {
                    return key;
                }

                @Override
                public String getCustomTranslation(String key) {
                    return key;
                }

                @Override
                public String getErrorCode(String messageId) {
                    return messageId;
                }

                @Override
                public String getTranslation(String key, String defaultValue) {
                    return key + defaultValue;
                }

                @Override
                public boolean hasTranslation(String key) {
                    return false;
                }
            };
        }
    }

    /**
     * Checks that the expected number of parameters match with the actual parameters.
     *
     * @param expectedNumberOfParameters The expected number of parameters
     * @param actualParameters The actual parameters
     *
     * @return A flag that indicates if the expected number of parameters match the number of actual parameters
     */
    private static boolean numberOfParametersMatch(int expectedNumberOfParameters, Object... actualParameters) {
        if (actualParameters == null) {
            return expectedNumberOfParameters == 0;
        }
        else {
            return actualParameters.length == expectedNumberOfParameters;
        }
    }

    public String getMessageId() {
        return messageId;
    }

    public Object[] getMessageArguments() {
        return messageArguments;
    }

}