package com.energyict.mdc.engine.exceptions;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.core.RunningComServer;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.protocol.api.exceptions.ComServerRuntimeException;

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
     * @return a newly created CodingException
     */
    public static CodingException methodArgumentCanNotBeNull(Class clazz, String methodName, String argument) {
        return new CodingException(MessageSeeds.METHOD_ARGUMENT_CAN_NOT_BE_NULL, clazz.getName(), methodName, argument);
    }

    /**
     * Constructs a new CodingException that represents that an attempt was made
     * to create a logger from a class that is NOT an interface.
     *
     * @param messageClass The Class that is NOT an interface
     * @return The CodingException
     */
    public static CodingException loggerFactoryRequiresInterface (Class messageClass) {
        return new CodingException(MessageSeeds.LOGGER_FACTORY_REQUIRES_INTERFACE, messageClass.getName());
    }

    /**
     * Constructs a new CodingException that represents that an attempt was made
     * to create a logger for an interface that contains at least one method
     * that has multiple exception parameter types while only 1 is supported.
     *
     * @param method The Method that has multiple exception paramter types
     * @return The CodingException
     */
    public static CodingException loggerFactorySupportsOnlyOneThrowableParameter (Method method) {
        return new CodingException(MessageSeeds.LOGGER_FACTORY_SUPPORTS_ONLY_ONE_THROWABLE_PARAMETER, method.toString());
    }

    public static CodingException validationFailed (Exception cause, String businessObjectClassName, String propertyName) {
        return new CodingException(cause, MessageSeeds.VALIDATION_FAILED, businessObjectClassName, propertyName);
    }

    /**
     * Constructs a new CodingException to represent that previous validation
     * method has failed. Note that this situation is detected before
     * another serious exception has occurred.
     *
     * @param businessObjectClass The business object class where validation failed
     * @param propertyName The name of the property on which validation failed
     * @return The CodingException
     */
    public static CodingException validationFailed (Class businessObjectClass, String propertyName) {
        return new CodingException(MessageSeeds.VALIDATION_FAILED, businessObjectClass.getName(), propertyName);
    }

    /**
     * Constructs a new CodingException to represent a failure
     * to recognize an enum value, most likely a switch branch that is missing in the code.
     *
     * @param enumValue The unrecognized enum value
     * @return The CodingException
     */
    public static <T extends Enum> CodingException unrecognizedEnumValue (T enumValue) {
        return new CodingException(MessageSeeds.UNRECOGNIZED_ENUM_VALUE, enumValue.getClass(), enumValue.ordinal());
    }

    /**
     * Constructs a new CodingException to represent a failure
     * to map an ordinal value stored in database back
     * to the enum value.
     *
     * @param enumClass The enum class
     * @param ordinalValue The ordinal value
     * @return The CodingException
     */
    public static <T extends Enum> CodingException unrecognizedEnumValue (Class<T> enumClass, int ordinalValue) {
        return new CodingException(MessageSeeds.UNRECOGNIZED_ENUM_VALUE, enumClass.getName(), ordinalValue);
    }

    /**
     * Constructs a CodingException to represent the scenario where the primaryKey
     * of a DeviceMessageSpec wasn't in the correct format.
     *
     * @param primaryKey the primary key with the incorrect format
     * @return the newly created CodingException
     */
    public static CodingException deviceMessageSpecPrimaryKeyNotCorrect(MessageSeed messageSeed, String primaryKey) {
        // Old message in 9.1: The DeviceMessageSpec should contain a className and a enumName, but was {0}
        return new CodingException(messageSeed, primaryKey);
    }

    /**
     * Constructs a CodingException to represent the scenario where the className of
     * the primary key was not known in the classPath
     *
     * @param e The ClassNotFoundException
     * @param className the unknown className
     * @return the newly created CodingException
     */
    public static CodingException unKnownDeviceMessageSpecClass(MessageSeed messageSeed, ClassNotFoundException e, String className) {
        // Old message in 9.1: Could not create the requested DeviceMessageSpec, class with name \"{0}\" is not on classpath
        return new CodingException(e, messageSeed, className);
    }

    /**
     * Constructs a CodingException to represent the scenario where the primaryKey
     * of a DeviceMessageCategory wasn't in the correct format.
     *
     * @param primaryKey the primary key with the incorrect format
     * @return the newly created CodingException
     */
    public static CodingException deviceMessageCategoryPrimaryKeyNotCorrect(MessageSeed messageSeed, String primaryKey) {
        // Old message in 9.1: Unexpected format for device message category primary key {0}. Expected only 2 chars
        return new CodingException(messageSeed, primaryKey);
    }

    /**
     * Constructs a CodingException to represent the scenario where the className of
     * the primary key was not known in the classPath.
     *
     * @param e The ClassNotFoundException
     * @param className the unknown className
     * @return the newly created CodingException
     */
    public static CodingException unKnownDeviceMessageCategoryClass(MessageSeed messageSeed, ClassNotFoundException e, String className) {
        // Old message in 9.1: Unknown device message category class name\: {0}
        return new CodingException(e, messageSeed, className);
    }

    /**
     * Constructs a CodingException that represents the fact that we have an unexpected amount
     * of ComTaskExecutions.
     *
     * @param expectedNumber the expected number of ComTaskExecutions
     * @param actualNumber the actual number of ComTaskExecutions
     * @return the CodingException
     */
    public static CodingException incorrectNumberOfPreparedComTaskExecutions(int expectedNumber, int actualNumber){
        return new CodingException(MessageSeeds.INCORRECT_NUMBER_OF_COMTASKS, expectedNumber, actualNumber);
    }

    /**
     * Constructs a new CodingException indicating there is no ComTaskExecutionSessionShadow
     * available while one is expected because the related ComTask has effectively executed.
     *
     * @param comTaskExecution The ComTaskExecution
     * @return The CodingException
     */
    public static CodingException comTaskSessionMissing (ComTaskExecution comTaskExecution) {
        return new CodingException(MessageSeeds.SESSION_FOR_COMTASK_MISSING, comTaskExecution.getComTasks().get(0).getName());
    }

    public static CodingException malformedObjectName (RunningComServer comServer, MalformedObjectNameException e) {
        return malformedObjectName(comServer.getComServer(), e);
    }

    public static CodingException malformedObjectName (ComServer comServer, MalformedObjectNameException e) {
        return new CodingException(e, MessageSeeds.MBEAN_OBJECT_FORMAT, comServer.getName());
    }

    public static CodingException malformedObjectName (ComPort comPort, MalformedObjectNameException e) {
        return new CodingException(e, MessageSeeds.MBEAN_OBJECT_FORMAT, comPort.getName());
    }

    public static CodingException compositeTypeCreation (Class clazz, OpenDataException e) {
        return new CodingException(e, MessageSeeds.COMPOSITE_TYPE_CREATION, clazz.getName());
    }

    public static CodingException compositeDataCreation (Class clazz, OpenDataException e) {
        return new CodingException(e, MessageSeeds.COMPOSITE_TYPE_CREATION, clazz.getName());
    }

    protected CodingException (MessageSeed messageSeed, Object... messageArguments) {
        super(messageSeed, messageArguments);
    }

    private CodingException (Throwable cause, MessageSeed messageSeed, Object... messageArguments) {
        super(cause, messageSeed, messageArguments);
    }

}