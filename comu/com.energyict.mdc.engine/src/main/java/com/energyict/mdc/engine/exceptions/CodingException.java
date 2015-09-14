package com.energyict.mdc.engine.exceptions;

import com.energyict.mdc.common.ComServerRuntimeException;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComServer;

import com.elster.jupiter.util.exception.MessageSeed;

import javax.management.MalformedObjectNameException;
import javax.management.openmbean.OpenDataException;
import java.lang.reflect.Method;

/**
 * Models the exceptional situations that occur when a developer has
 * forgotten or neglected to comply with coding standards
 * or constraints imposed by model components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-08-23 (12:29)
 */
public class CodingException extends ComServerRuntimeException {

    /**
     * Coding Exception created if some method argument was NULL when it shouldn't be
     *
     * @param clazz      the {@link Class} where the coding error occurred
     * @param methodName the method name in the class where the null argument appeared
     * @param argument   the name of the argument which was null
     * @param messageSeed The MessageSeed
     * @return a newly created CodingException
     */
    public static CodingException methodArgumentCanNotBeNull(Class clazz, String methodName, String argument, MessageSeed messageSeed) {
        return new CodingException(messageSeed, clazz.getName(), methodName, argument);
    }

    /**
     * Constructs a new CodingException that represents that an attempt was made
     * to create a logger from a class that is NOT an interface.
     *
     * @param messageClass The Class that is NOT an interface
     * @param messageSeed The MessageSeed
     * @return The CodingException
     */
    public static CodingException loggerFactoryRequiresInterface(Class messageClass, MessageSeed messageSeed) {
        return new CodingException(messageSeed, messageClass.getName());
    }

    /**
     * Constructs a new CodingException that represents that an attempt was made
     * to create a logger for an interface that contains at least one method
     * that has multiple exception parameter types while only 1 is supported.
     *
     * @param method The Method that has multiple exception paramter types
     * @param messageSeed The MessageSeed
     * @return The CodingException
     */
    public static CodingException loggerFactorySupportsOnlyOneThrowableParameter(Method method, MessageSeed messageSeed) {
        return new CodingException(messageSeed, method.toString());
    }

    public static CodingException validationFailed(Exception cause, String businessObjectClassName, String propertyName, MessageSeed messageSeed) {
        return new CodingException(cause, messageSeed, businessObjectClassName, propertyName);
    }

    /**
     * Constructs a new CodingException to represent that previous validation
     * method has failed. Note that this situation is detected before
     * another serious exception has occurred.
     *
     * @param businessObjectClass The business object class where validation failed
     * @param propertyName The name of the property on which validation failed
     * @param messageSeed The MessageSeed
     * @return The CodingException
     */
    public static CodingException validationFailed(Class businessObjectClass, String propertyName, MessageSeed messageSeed) {
        return new CodingException(messageSeed, businessObjectClass.getName(), propertyName);
    }

    /**
     * Constructs a new CodingException to represent a failure
     * to recognize an enum value, most likely a switch branch that is missing in the code.
     *
     * @param enumValue The unrecognized enum value
     * @param messageSeed
     * @return The CodingException
     */
    public static <T extends Enum> CodingException unrecognizedEnumValue(T enumValue, MessageSeed messageSeed) {
        return new CodingException(messageSeed, enumValue.getClass(), enumValue.ordinal());
    }

    /**
     * Constructs a new CodingException to represent a failure
     * to map an ordinal value stored in database back
     * to the enum value.
     *
     * @param enumClass The enum class
     * @param ordinalValue The ordinal value
     * @param messageSeed The MessageSeed
     * @return The CodingException
     */
    public static <T extends Enum> CodingException unrecognizedEnumValue(Class<T> enumClass, int ordinalValue, MessageSeed messageSeed) {
        return new CodingException(messageSeed, enumClass.getName(), ordinalValue);
    }

    /**
     * Constructs a CodingException that represents the fact that we have an unexpected amount
     * of ComTaskExecutions.
     *
     * @param expectedNumber the expected number of ComTaskExecutions
     * @param actualNumber the actual number of ComTaskExecutions
     * @param messageSeed The MessageSeed
     * @return the CodingException
     */
    public static CodingException incorrectNumberOfPreparedComTaskExecutions(int expectedNumber, int actualNumber, MessageSeed messageSeed){
        return new CodingException(messageSeed, expectedNumber, actualNumber);
    }

    /**
     * Constructs a new CodingException indicating there is no ComTaskExecutionSessionShadow
     * available while one is expected because the related ComTask has effectively executed.
     *
     * @param comTaskExecution The ComTaskExecution
     * @param messageSeed The The MessageSeed
     * @return The CodingException
     */
    public static CodingException comTaskSessionMissing(ComTaskExecution comTaskExecution, MessageSeed messageSeed) {
        return new CodingException(messageSeed, comTaskExecution.getComTasks().get(0).getName());
    }

    public static CodingException malformedObjectName(ComServer comServer, MalformedObjectNameException e, MessageSeed messageSeed) {
        return malformedComServerObjectName(comServer.getName(), e, messageSeed);
    }

    public static CodingException malformedComServerObjectName(String comServerName, MalformedObjectNameException e, MessageSeed messageSeed) {
        return new CodingException(e, messageSeed, comServerName);
    }

    public static CodingException malformedObjectName(ComPort comPort, MalformedObjectNameException e, MessageSeed messageSeed) {
        return new CodingException(e, messageSeed, comPort.getName());
    }

    public static CodingException compositeTypeCreation(Class clazz, OpenDataException e, MessageSeed messageSeed) {
        return new CodingException(e, messageSeed, clazz.getName());
    }

    public static CodingException compositeDataCreation(Class clazz, OpenDataException e, MessageSeed messageSeed) {
        return new CodingException(e, messageSeed, clazz.getName());
    }

    protected CodingException (MessageSeed messageSeed, Object... messageArguments) {
        super(messageSeed, messageArguments);
    }

    private CodingException (Throwable cause, MessageSeed messageSeed, Object... messageArguments) {
        super(cause, messageSeed, messageArguments);
    }

}