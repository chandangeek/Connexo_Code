package com.energyict.protocol.exceptions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;

/**
 * Serves as the root for all exceptions related to the execution of the DeviceProtocols
 *
 * @author sva
 * @since 9/10/2015 - 11:03
 */
public class ProtocolRuntimeException extends RuntimeException {

    private Object[] messageArguments;
    private ProtocolExceptionReference exceptionReference;

    public ProtocolRuntimeException(ProtocolExceptionReference exceptionReference, Object... messageArguments) {
        super(formattedMessage(exceptionReference, messageArguments));
        this.exceptionReference = exceptionReference;
        this.messageArguments = messageArguments;
        assert numberOfParametersMatch(exceptionReference.getExpectedNumberOfArguments(), messageArguments) : "Wrong number of arguments for exception message " + exceptionReference;
    }

    public ProtocolRuntimeException(Throwable cause, ProtocolExceptionReference exceptionReference, Object... messageArguments) {
        super(formattedMessage(exceptionReference, messageArguments), cause);
        this.exceptionReference = exceptionReference;
        this.messageArguments = messageArguments;
        assert numberOfParametersMatch(exceptionReference.getExpectedNumberOfArguments(), messageArguments) : "Wrong number of arguments for exception message " + exceptionReference;
    }

    /**
     * Checks that the expected number of parameters match with the actual parameters.
     *
     * @param expectedNumberOfParameters The expected number of parameters
     * @param actualParameters           The actual parameters
     * @return A flag that indicates if the expected number of parameters match the number of actual parameters
     */
    private static boolean numberOfParametersMatch(int expectedNumberOfParameters, Object... actualParameters) {
        if (actualParameters == null) {
            return expectedNumberOfParameters == 0;
        } else {
            return actualParameters.length == expectedNumberOfParameters;
        }
    }

    public ProtocolExceptionReference getExceptionReference() {
        return this.exceptionReference;
    }

    public Object[] getMessageArguments() {
        return messageArguments;
    }

    private static String formattedMessage(ProtocolExceptionReference exceptionReference, Object[] messageArguments) {
        String msgKey = "PRTCL-" + exceptionReference.toNumerical();
        String msgPattern;
        try {
            Class<?> cls = Class.forName("com.energyict.cuo.core.UserEnvironment");
            Method method = cls.getDeclaredMethod("getErrorMsg", String.class);
            msgPattern = "[" + msgKey + "] " + method.invoke(cls.newInstance(), msgKey);
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            // In case reflection failed, fall back to default message format
            msgPattern = "[" + msgKey + "] " + exceptionReference.getMessageFormat();
        }

        return MessageFormat.format(msgPattern.replaceAll("'", "''"), messageArguments);
    }
}