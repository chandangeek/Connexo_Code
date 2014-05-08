package com.energyict.mdc.engine.exceptions;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.exceptions.CommonExceptionReferences;
import com.energyict.mdc.common.exceptions.CommonReferenceScope;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.exceptions.ComServerRuntimeException;
import com.energyict.mdc.common.exceptions.ExceptionCode;
import com.energyict.mdc.common.exceptions.ExceptionType;

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
public final class CodingException extends ComServerRuntimeException {

    /**
     * Constructs a new CodingException to represent that a second {@link DeviceCommand}
     * was found that needs to be executed ExecutionTimePreference#FIRST.
     *
     * @param first The existing DeviceCommand that wants to execute first
     * @param second The second DeviceCommand that wants to execute first
     * @return The CodingException
     */
    public static CodingException onlyOneFirstDeviceCommand (DeviceCommand first, DeviceCommand second) {
        return new CodingException(
                onlyOneDeviceCommandCode(ComServerModelExceptionReferences.ONLY_ONE_FIRST_DEVICE_COMMAND),
                first,
                second);
    }

    /**
     * Constructs a new CodingException to represent that a second {@link DeviceCommand}
     * was found that needs to be executed ExecutionTimePreference#LAST.
     *
     * @param last The existing DeviceCommand that wants to execute last
     * @param second The second DeviceCommand that wants to execute last
     * @return The CodingException
     */
    public static CodingException onlyOneLastDeviceCommand (DeviceCommand last, DeviceCommand second) {
        return new CodingException(
                onlyOneDeviceCommandCode(ComServerModelExceptionReferences.ONLY_ONE_LAST_DEVICE_COMMAND),
                last,
                second);
    }

    /**
     * Constructs a new CodingException to represent an error produced
     * by the java reflection layer (wrapped by a {@link com.energyict.mdc.common.BusinessException})
     * when an attempt was made to create a new instance of some PluggableClass.
     *
     * @param reflectionErrorWrapper The BusinessException that wraps the java reflection layer exception
     *                               which could (or should) be one of
     *                               ClassNotFoundException,
     *                               InstantiationException,
     *                               IllegalAccessException,
     *                               NoClassDefFoundError
     *                               or even ClassCastException when the created object was not even a Pluggable.
     * @param pluggableClass The Pluggable class
     * @return The CodingException
     */
    public static CodingException reflectionError (BusinessException reflectionErrorWrapper, PluggableClass pluggableClass) {
        return new CodingException(reflectionErrorWrapper.getCause(), reflectionErrorExceptionCode(), pluggableClass.getPluggableClassType().name(), pluggableClass.getJavaClassName());
    }

    /**
     * Constructs a new CodingException to represent an error produced
     * by the java reflection layer (wrapped by a {@link BusinessException})
     * when an attempt was made to create a new instance of some {@link com.energyict.mdc.protocol.api.DeviceProtocolDialect}.
     *
     * @param reflectionError The java reflection layer exception which could (or should) be one of
     *                        ClassNotFoundException,
     *                        InstantiationException,
     *                        IllegalAccessException,
     *                        NoClassDefFoundError
     *                        or even ClassCastException when the created object was not even a DeviceProtocolDialect.
     * @param deviceProtocolDialectClassName The device protocol dialect class name
     * @return The CodingException
     */
    public static CodingException protocolReflectionError (Throwable reflectionError, String deviceProtocolDialectClassName) {
        String protocolDialect = Environment.DEFAULT.get().getTranslation("protocolDialect");
        return new CodingException(reflectionError, reflectionErrorExceptionCode(), protocolDialect, deviceProtocolDialectClassName);
    }

    public static CodingException genericReflectionError(Exception reflectionError, Class someClass) {
        return genericReflectionError(reflectionError, someClass.getName());
    }

    public static CodingException genericReflectionError(Exception reflectionError, String className) {
        return new CodingException(reflectionError, genericReflectionErrorExceptionCode(), className);
    }

    private static ExceptionCode reflectionErrorExceptionCode () {
        return new ExceptionCode(new CommonReferenceScope(), ExceptionType.CODING, CommonExceptionReferences.JAVA_REFLECTION_ERROR);
    }

    private static ExceptionCode genericReflectionErrorExceptionCode(){
        return new ExceptionCode(new CommonReferenceScope(), ExceptionType.CODING, CommonExceptionReferences.GENERIC_JAVA_REFLECTION_ERROR);
    }

    /**
     * Coding Exception created if some method is not supported for this class
     *
     * @param clazz      the {@link Class} where the coding error occurred
     * @param methodName the method name of the unsupported method
     * @return a newly created CodingException
     */
    public static CodingException unsupportedMethod(Class clazz, String methodName) {
        return new CodingException(methodNotSupported(), clazz.getName(), methodName);
    }

    private static ExceptionCode methodNotSupported() {
        return new ExceptionCode(new CommonReferenceScope(), ExceptionType.CODING, CommonExceptionReferences.UNSUPPORTED_METHOD);
    }

    /**
     * Coding Exception created if some method argument was NULL when it shouldn't be
     *
     * @param clazz      the {@link Class} where the coding error occurred
     * @param methodName the method name in the class where the null argument appeared
     * @param argument   the name of the argument which was null
     * @return a newly created CodingException
     */
    public static CodingException methodArgumentCanNotBeNull(Class clazz, String methodName, String argument) {
        return new CodingException(argumentCanNotBeNull(), clazz.getName(), methodName, argument);
    }

    private static ExceptionCode argumentCanNotBeNull() {
        return new ExceptionCode(new CommonReferenceScope(), ExceptionType.CODING, CommonExceptionReferences.METHOD_ARGUMENT_CAN_NOT_BE_NULL);
    }

    /**
     * Constructs a new CodingException that represents that an attempt was made
     * to create a logger from a class that is NOT an interface.
     *
     * @param messageClass The Class that is NOT an interface
     * @return The CodingException
     */
    public static CodingException loggerFactoryRequiresInterface (Class messageClass) {
        return new CodingException(loggerFactoryRequiresInterface(), messageClass.getName());
    }

    private static ExceptionCode loggerFactoryRequiresInterface () {
        return new ExceptionCode(new CommonReferenceScope(), ExceptionType.CODING, CommonExceptionReferences.LOGGER_FACTORY_REQUIRES_INTERFACE);
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
        return new CodingException(loggerFactorySupportsOnlyOneThrowableParameter(), method.toString());
    }

    private static ExceptionCode loggerFactorySupportsOnlyOneThrowableParameter () {
        return new ExceptionCode(new CommonReferenceScope(), ExceptionType.CODING, CommonExceptionReferences.LOGGER_FACTORY_SUPPORTS_ONLY_ONE_THROWABLE_PARAMETER);
    }

    /**
     * Constructs a new CodingException to represent that previous validation
     * method has failed and is now causing the specified exception.
     *
     * @param cause The exception that occurred as an effect of the failing validation
     * @param businessObjectClass The business object class where validation failed
     * @param propertyName The name of the property on which validation failed
     * @return The CodingException
     */
    public static CodingException validationFailed (Exception cause, Class businessObjectClass, String propertyName) {
        return new CodingException(cause, validationFailedCode(), businessObjectClass.getName(), propertyName);
    }

    public static CodingException validationFailed (Exception cause, String businessObjectClassName, String propertyName) {
        return new CodingException(cause, validationFailedCode(), businessObjectClassName, propertyName);
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
        return new CodingException(validationFailedCode(), businessObjectClass.getName(), propertyName);
    }

    private static ExceptionCode validationFailedCode () {
        return new ExceptionCode(new CommonReferenceScope(), ExceptionType.CODING, CommonExceptionReferences.VALIDATION_FAILED);
    }

    /**
     * Constructs a new CodingException to represent a failure
     * to recognize an enum value, most likely a switch branch that is missing in the code.
     *
     * @param enumValue The unrecognized enum value
     * @return The CodingException
     */
    public static <T extends Enum> CodingException unrecognizedEnumValue (T enumValue) {
        return new CodingException(unrecognizedEnumValueCode(), enumValue.getClass(), enumValue.ordinal());
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
        return new CodingException(unrecognizedEnumValueCode(), enumClass.getName(), ordinalValue);
    }

    private static ExceptionCode unrecognizedEnumValueCode () {
        return new ExceptionCode(new CommonReferenceScope(), ExceptionType.CODING, CommonExceptionReferences.UNRECOGNIZED_ENUM_VALUE);
    }

    /**
     * Constructs a new CodingException to represent a failure
     * to recognize an object as a supported business object factory.
     *
     * @param businesObjectFactory The unexpected business object factory
     * @return The CodingException
     */
    public static CodingException unsupportedFactory (Object businesObjectFactory) {
        return new CodingException(unsupportedFactoryCode(), businesObjectFactory.getClass().getName());
    }

    private static ExceptionCode unsupportedFactoryCode () {
        return new ExceptionCode(new CommonReferenceScope(), ExceptionType.CODING, CommonExceptionReferences.UNEXPECTED_FACTORY);
    }

    /**
     * Constructs a new CodingException to represent the detection of
     * asynchroneous communication when that is not supported.
     *
     * @return The CodingException
     */
    public static CodingException asynchroneousCommunicationIsNotSupported () {
        return new CodingException(asynchroneousCommunicationIsNotSupportedCode());
    }

    private static ExceptionCode asynchroneousCommunicationIsNotSupportedCode () {
        return new ExceptionCode(new CommonReferenceScope(), ExceptionType.CODING, CommonExceptionReferences.ASYNCHRONEOUS_COMMUNICATION_IS_NOT_SUPPORTED);
    }

    /**
     * Constructs a CodingException to represent the scenario where the primaryKey
     * of a DeviceMessageSpec wasn't in the correct format.
     *
     * @param primaryKey the primary key with the incorrect format
     * @return the newly created CodingException
     */
    public static CodingException deviceMessageSpecPrimaryKeyNotCorrect(String primaryKey) {
        return new CodingException(deviceMessageSpecPrimaryKeyNotCorrectCode(), primaryKey);
    }

    private static ExceptionCode deviceMessageSpecPrimaryKeyNotCorrectCode() {
        return new ExceptionCode(new CommonReferenceScope(), ExceptionType.CODING, CommonExceptionReferences.INCORRECT_DEVICE_MESSAGE_SPEC_PRIMARY_KEY);
    }

    /**
     * Constructs a CodingException to represent the scenario where the className of
     * the primary key was not known in the classPath
     *
     * @param e The ClassNotFoundException
     * @param className the unknown className
     * @return the newly created CodingException
     */
    public static CodingException unKnownDeviceMessageSpecClass(ClassNotFoundException e, String className) {
        return new CodingException(e, unKnownDeviceMessageSpecClassExceptionCode(), className);
    }

    private static ExceptionCode unKnownDeviceMessageSpecClassExceptionCode() {
        return new ExceptionCode(new CommonReferenceScope(), ExceptionType.CODING, CommonExceptionReferences.UNKNOWN_DEVICE_MESSAGE_SPEC_CLASS);
    }

    /**
     * Constructs a CodingException to represent the scenario where the className of
     * a {@link com.energyict.mdc.protocol.api.legacy.dynamic.ValueFactory lecacy value factory} is unknown or no longer supported.
     *
     * @param className the unknown className
     * @return the newly created CodingException
     */
    public static CodingException unKnownLegacyValueFactoryClass(String className) {
        return new CodingException(unKnownLegacyValueFactoryClassExceptionCode(), className);
    }

    private static ExceptionCode unKnownLegacyValueFactoryClassExceptionCode() {
        return new ExceptionCode(new CommonReferenceScope(), ExceptionType.CODING, CommonExceptionReferences.UNKNOWN_LEGACY_VALUEFACTORY_CLASS);
    }

    /**
     * Constructs a CodingException to represent the scenario where the primaryKey
     * of a DeviceMessageCategory wasn't in the correct format.
     *
     * @param primaryKey the primary key with the incorrect format
     * @return the newly created CodingException
     */
    public static CodingException deviceMessageCategoryPrimaryKeyNotCorrect(String primaryKey) {
        return new CodingException(deviceMessageCategoryPrimaryKeyNotCorrectCode(), primaryKey);
    }

    private static ExceptionCode deviceMessageCategoryPrimaryKeyNotCorrectCode() {
        return new ExceptionCode(new CommonReferenceScope(), ExceptionType.CODING, CommonExceptionReferences.INCORRECT_DEVICE_MESSAGE_CATEGORY_PRIMARY_KEY);
    }


    /**
     * Constructs a CodingException to represent the scenario where the className of
     * the primary key was not known in the classPath.
     *
     * @param e The ClassNotFoundException
     * @param className the unknown className
     * @return the newly created CodingException
     */
    public static CodingException unKnownDeviceMessageCategoryClass(ClassNotFoundException e, String className) {
        return new CodingException(e, unKnownDeviceMessageCategoryClassExceptionCode(), className);
    }

    private static ExceptionCode unKnownDeviceMessageCategoryClassExceptionCode() {
        return new ExceptionCode(new CommonReferenceScope(), ExceptionType.CODING, CommonExceptionReferences.UNKNOWN_DEVICE_MESSAGE_CATEGORY_CLASS);
    }

    private static ExceptionCode unknownPartialConnectionTaskShadowClassExceptionCode () {
        return new ExceptionCode(new CommonReferenceScope(), ExceptionType.CODING, CommonExceptionReferences.UNKNOWN_PARTIAL_CONNECTION_TASK_SHADOW);
    }

    /**
     * Constructs a CodingException that represents a failure to marshall
     * the specified object to JSon via the Jaxb annotations provided
     * by the object's class.
     *
     * @param anObject The object that failed to marshall
     * @param e The cause
     * @return The CodingException
     */
    public static CodingException jsonWithJaxbMarshallingFailed (Object anObject, Exception e) {
        return new CodingException(e, jsonWithJaxbMarshallingFailedExceptionCode(), anObject.getClass().getName());
    }

    private static ExceptionCode jsonWithJaxbMarshallingFailedExceptionCode () {
        return new ExceptionCode(new CommonReferenceScope(), ExceptionType.CODING, CommonExceptionReferences.UNEXPECTED_XML_JSON_ERROR);
    }

    /**
     * Constructs a CodingException that represents the fact that
     * a property name was provided to a {@link com.energyict.mdc.device.config.SecurityPropertySet}
     * but that SecurityPropertySet does not support a property by that name.
     *
     * @param securityPropertySet The SecurityPropertySet
     * @param propertyName The name of the unknown property
     * @return The CodingException
     */
    public static CodingException unsupportedSecurityProperty (SecurityPropertySet securityPropertySet, String propertyName) {
        return new CodingException(unsupportedSecurityPropertyExceptionCode(), securityPropertySet.getName(), propertyName);
    }

    private static ExceptionCode unsupportedSecurityPropertyExceptionCode () {
        return new ExceptionCode(new CommonReferenceScope(), ExceptionType.CODING, CommonExceptionReferences.UNSUPPORTED_SECURITY_PROPERTY);
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
        return new CodingException(incorrectNumberOfPreparedComTaskExceptionCode(), expectedNumber, actualNumber);
    }

    private static ExceptionCode incorrectNumberOfPreparedComTaskExceptionCode() {
        return new ExceptionCode(new CommonReferenceScope(), ExceptionType.CODING, CommonExceptionReferences.INCORRECT_NUMBER_OF_COMTASKS);
    }

    /**
     * Constructs a CodingException that wraps an unexpected BusinessException.
     *
     * @param e The cause
     * @return The CodingException
     */
    public static CodingException unexpectedBusinessException (BusinessException e) {
        return new CodingException(e, unexpectedBusinessExceptionExceptionCode());
    }

    private static ExceptionCode unexpectedBusinessExceptionExceptionCode () {
        return new ExceptionCode(new CommonReferenceScope(), ExceptionType.CODING, CommonExceptionReferences.UNEXPECTED_BUSINESS_EXCEPTION);
    }

    /**
     * Constructs a CodingException, indicating no pluggable classes could be found for the given connection type
     *
     * @param connectionTypeClass   the class of the ConnectionType
     * @param <T>
     * @return
     */
    public static <T extends ConnectionType> CodingException noPluggableClassesFoundForConnectionType(Class<T> connectionTypeClass) {
        return new CodingException(noPluggableClassesFoundForConnectionTypeExceptionCode(), connectionTypeClass.getName());
    }

    private static ExceptionCode noPluggableClassesFoundForConnectionTypeExceptionCode () {
        return new ExceptionCode(new CommonReferenceScope(), ExceptionType.CODING, CommonExceptionReferences.NO_PLUGGABLE_CLASSES_FOUND_FOR_CONNECTION_TYPE);
    }

    public static CodingException noConnectionTypePluggableClass(PluggableClass pluggableClass) {
        return new CodingException(noConnectionTypePluggableClassExceptionCode(), pluggableClass.getName());
    }

    public static CodingException noConnectionTypePluggableClass(BusinessException e, PluggableClass pluggableClass) {
        return new CodingException(e, noConnectionTypePluggableClassExceptionCode(), pluggableClass.getName());
    }

    private static ExceptionCode noConnectionTypePluggableClassExceptionCode () {
        return new ExceptionCode(new CommonReferenceScope(), ExceptionType.CODING, CommonExceptionReferences.NO_CONNECTION_TYPE_PLUGGABLE_CLASS);
    }

    /**
     * Constructs a CodingException, indicating the property spec of a certain property of a connection type could not be found
     *
     * @param connectionTypeClass   the class of the ConnectionType
     * @param propertyName          the name of the property
     * @param <T>
     * @return
     */
    public static <T extends ConnectionType> CodingException connectionTypePropertySpecNotFound (Class<T> connectionTypeClass, String propertyName) {
        return new CodingException(connectionTypePropertySpecNotFoundExceptionCode(), connectionTypeClass.getName(), propertyName);
    }

    /**
     * Constructs a new CodingException indicating there is no ComTaskExecutionSessionShadow
     * available while one is expected because the related ComTask has effectively executed.
     *
     * @param comTaskExecution The ComTaskExecution
     * @return The CodingException
     */
    public static CodingException comTaskSessionMissing (ComTaskExecution comTaskExecution) {
        return new CodingException(comTaskSessionMissingExceptionCode(), comTaskExecution.getComTask().getName());
    }

    public static CodingException malformedObjectName (RunningComServer comServer, MalformedObjectNameException e) {
        return new CodingException(e, malformedObjectNameExceptionCode(), comServer.getComServer().getName());
    }

    private static ExceptionCode malformedObjectNameExceptionCode () {
        return new ExceptionCode(new com.energyict.mdc.engine.exceptions.CommonReferenceScope(), ExceptionType.CODING, com.energyict.mdc.engine.exceptions.CommonExceptionReferences.MBEAN_OBJECT_FORMAT);
    }

    public static CodingException compositeTypeCreation (Class clazz, OpenDataException e) {
        return new CodingException(e, compositeTypeCreationExceptionCode(), clazz.getName());
    }

    private static ExceptionCode compositeTypeCreationExceptionCode () {
        return new ExceptionCode(new com.energyict.mdc.engine.exceptions.CommonReferenceScope(), ExceptionType.CODING, com.energyict.mdc.engine.exceptions.CommonExceptionReferences.COMPOSITE_TYPE_CREATION);
    }

    public static CodingException compositeDataCreation (Class clazz, OpenDataException e) {
        return new CodingException(e, compositeDataCreationExceptionCode(), clazz.getName());
    }

    private static ExceptionCode compositeDataCreationExceptionCode () {
        return new ExceptionCode(new com.energyict.mdc.engine.exceptions.CommonReferenceScope(), ExceptionType.CODING, com.energyict.mdc.engine.exceptions.CommonExceptionReferences.COMPOSITE_TYPE_CREATION);
    }

    public static CodingException unknownCompositeDataItem (Class clazz, String itemName) {
        return new CodingException(unknownCompositeDataItemExceptionCode(), clazz.getName(), itemName);
    }

    private static ExceptionCode unknownCompositeDataItemExceptionCode () {
        return new ExceptionCode(new com.energyict.mdc.engine.exceptions.CommonReferenceScope(), ExceptionType.CODING, com.energyict.mdc.engine.exceptions.CommonExceptionReferences.UNKNOWN_COMPOSITE_DATA_ITEM);
    }


    private static ExceptionCode comTaskSessionMissingExceptionCode () {
        return new ExceptionCode(new CommonReferenceScope(), ExceptionType.CODING, CommonExceptionReferences.SESSION_FOR_COMTASK_MISSING);
    }

    private static ExceptionCode connectionTypePropertySpecNotFoundExceptionCode () {
        return new ExceptionCode(new CommonReferenceScope(), ExceptionType.CODING, CommonExceptionReferences.CONNECTION_TYPE_PROPERTY_SPEC_NOT_FOUND);
    }


    private static ExceptionCode onlyOneDeviceCommandCode (ComServerModelExceptionReferences exceptionReference) {
        return new ExceptionCode(new ComServerModelReferenceScope(), ExceptionType.CODING, exceptionReference);
    }

    private CodingException (ExceptionCode code, Object... messageArguments) {
        super(code, messageArguments);
    }

    private CodingException (Throwable cause, ExceptionCode code, Object... messageArguments) {
        super(cause, code, messageArguments);
    }

}