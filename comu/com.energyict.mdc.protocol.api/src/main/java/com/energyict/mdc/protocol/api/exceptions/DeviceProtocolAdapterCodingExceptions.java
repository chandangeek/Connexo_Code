package com.energyict.mdc.protocol.api.exceptions;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.ComServerRuntimeException;
import com.energyict.mdc.protocol.api.DeviceSecuritySupport;
import com.energyict.mdc.protocol.api.services.UnableToCreateProtocolInstance;

/**
 * Coding or setup exceptions which can occur in the adapter classes.
 * <p/>
 * Copyrights EnergyICT
 * Date: 14/01/13
 * Time: 11:43
 */
public final class DeviceProtocolAdapterCodingExceptions extends ComServerRuntimeException {

    /**
     * Constructs a new DeviceProtocolAdapterCodingExceptions to represent an error produced
     * by the java reflection layer when an attempt was made to create a new instance of a {@link DeviceSecuritySupport}
     *
     * @param classNotFound    The ClassNotFoundException that wraps the java reflection layer exception
     * @param noClassFoundName the name of the class that was not found on the classPath
     * @return The CodingException
     */
    public static DeviceProtocolAdapterCodingExceptions unKnownDeviceSecuritySupportClass(MessageSeed messageSeed, ClassNotFoundException classNotFound, String noClassFoundName) {
        return new DeviceProtocolAdapterCodingExceptions(classNotFound.getCause(), messageSeed, noClassFoundName);
    }

    public static DeviceProtocolAdapterCodingExceptions unKnownDeviceSecuritySupportClass(MessageSeed messageSeed, ProtocolCreationException e, String noClassFoundName) {
        if (e.getCause() == null) {
            return new DeviceProtocolAdapterCodingExceptions(e, messageSeed, noClassFoundName);
        }
        else {
            return new DeviceProtocolAdapterCodingExceptions(e.getCause(), messageSeed, noClassFoundName);
        }
    }

    /**
     * Constructs a new DeviceProtocolAdapterCodingExceptions to represent an error produced
     * by the java reflection layer when an attempt was made to create a new instance of a LegacyMessageConverter.
     *
     * @param cause The actual failure
     * @param className the name of the class for which instance creation failed
     * @return The CodingException
     */
    public static DeviceProtocolAdapterCodingExceptions deviceMessageConverterClassCreationFailure(MessageSeed messageSeed, UnableToCreateProtocolInstance cause, String className) {
        return new DeviceProtocolAdapterCodingExceptions(cause.getCause(), messageSeed, className);
    }

    /**
     * Constructs a new DeviceProtocolAdapterCodingExceptions which can be used in a scenario where
     * a {@link java.util.Map} element could not be found.
     *
     * @param clazz       the class in which the error occurred
     * @param mapName     the name of the map in that class
     * @param nonExisting the element that does not exist
     * @return the DeviceProtocolAdapterCodingExceptions
     */
    public static DeviceProtocolAdapterCodingExceptions mappingElementDoesNotExist(MessageSeed messageSeed, Class clazz, String mapName, String nonExisting) {
        return new DeviceProtocolAdapterCodingExceptions(messageSeed, clazz.getSimpleName(), mapName, nonExisting);
    }

    private DeviceProtocolAdapterCodingExceptions(MessageSeed messageSeed, Object... messageArguments) {
        super(messageSeed, messageArguments);
    }

    private DeviceProtocolAdapterCodingExceptions(Throwable cause, MessageSeed messageSeed, Object... messageArguments) {
        super(cause, messageSeed, messageArguments);
    }

    public static DeviceProtocolAdapterCodingExceptions genericReflectionError(MessageSeed messageSeed, Exception reflectionError, Class someClass) {
        return genericReflectionError(messageSeed, reflectionError, someClass.getName());
    }

    public static DeviceProtocolAdapterCodingExceptions genericReflectionError(MessageSeed messageSeed, Exception reflectionError, String className) {
        return new DeviceProtocolAdapterCodingExceptions(reflectionError, messageSeed, className);
    }

    /**
     * Coding Exception created if some method is not supported for this class
     *
     * @param clazz      the {@link Class} where the coding error occurred
     * @param methodName the method name of the unsupported method
     * @return a newly created DeviceProtocolAdapterCodingExceptions
     */
    public static DeviceProtocolAdapterCodingExceptions unsupportedMethod(MessageSeed messageSeed, Class clazz, String methodName) {
        return new DeviceProtocolAdapterCodingExceptions(messageSeed, clazz.getName(), methodName);
    }

}
