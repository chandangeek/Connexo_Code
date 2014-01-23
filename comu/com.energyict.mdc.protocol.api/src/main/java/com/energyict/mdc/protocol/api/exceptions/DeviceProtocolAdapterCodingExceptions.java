package com.energyict.mdc.protocol.api.exceptions;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.exceptions.CommonExceptionReferences;
import com.energyict.mdc.common.exceptions.CommonReferenceScope;
import com.energyict.mdc.protocol.api.DeviceSecuritySupport;
import com.energyict.mdc.common.exceptions.ExceptionCode;
import com.energyict.mdc.common.exceptions.ExceptionType;

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
     * by the java reflection layer (wrapped by a {@link BusinessException})
     * when an attempt was made to create a new instance of a {@link DeviceSecuritySupport}
     *
     * @param classNotFound    The ClassNotFoundException that wraps the java reflection layer exception
     * @param noClassFoundName the name of the class that was not found on the classPath
     * @return The CodingException
     */
    public static DeviceProtocolAdapterCodingExceptions unKnownDeviceSecuritySupportClass(ClassNotFoundException classNotFound, String noClassFoundName) {
        return new DeviceProtocolAdapterCodingExceptions(classNotFound.getCause(), unKnownDeviceSecuritySupportErrorExceptionCode(), noClassFoundName);
    }

    public static DeviceProtocolAdapterCodingExceptions unKnownDeviceSecuritySupportClass(ProtocolCreationException e, String noClassFoundName) {
        if (e.getCause() == null) {
            return new DeviceProtocolAdapterCodingExceptions(e, unKnownDeviceSecuritySupportErrorExceptionCode(), noClassFoundName);
        }
        else {
            return new DeviceProtocolAdapterCodingExceptions(e.getCause(), unKnownDeviceSecuritySupportErrorExceptionCode(), noClassFoundName);
        }
    }

    private static ExceptionCode unKnownDeviceSecuritySupportErrorExceptionCode() {
        return new ExceptionCode(new CommonReferenceScope(), ExceptionType.CODING, CommonExceptionReferences.UNKNOWN_DEVICE_SECURITY_SUPPORT_CLASS);
    }

    /**
     * Constructs a new DeviceProtocolAdapterCodingExceptions to represent an error produced
     * by the java reflection layer (wrapped by a {@link BusinessException})
     * when an attempt was made to create a new instance of a LegacyMessageConverter
     *
     * @param classNotFound    The ClassNotFoundException that wraps the java reflection layer exception
     * @param noClassFoundName the name of the class that was not found on the classPath
     * @return The CodingException
     */
    public static DeviceProtocolAdapterCodingExceptions unKnownDeviceMessageConverterClass(ClassNotFoundException classNotFound, String noClassFoundName) {
        return new DeviceProtocolAdapterCodingExceptions(classNotFound.getCause(), unKnownDeviceMessageConverterErrorExceptionCode(), noClassFoundName);
    }

    private static ExceptionCode unKnownDeviceMessageConverterErrorExceptionCode() {
        return new ExceptionCode(new CommonReferenceScope(), ExceptionType.CODING, CommonExceptionReferences.UNKNOWN_DEVICE_MESSAGE_CONVERTER_CLASS);
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
    public static DeviceProtocolAdapterCodingExceptions mappingElementDoesNotExist(Class clazz, String mapName, String nonExisting) {
        return new DeviceProtocolAdapterCodingExceptions(mappingElementDoesNotExistCode(), clazz.getSimpleName(), mapName, nonExisting);
    }

    private static ExceptionCode mappingElementDoesNotExistCode() {
        return new ExceptionCode(new CommonReferenceScope(), ExceptionType.CODING, CommonExceptionReferences.NON_EXISTING_MAP_ELEMENT);
    }

    private DeviceProtocolAdapterCodingExceptions(ExceptionCode code, Object... messageArguments) {
        super(code, messageArguments);
    }

    private DeviceProtocolAdapterCodingExceptions(Throwable cause, ExceptionCode code, Object... messageArguments) {
        super(cause, code, messageArguments);
    }

    public static DeviceProtocolAdapterCodingExceptions genericReflectionError(Exception reflectionError, Class someClass) {
        return genericReflectionError(reflectionError, someClass.getName());
    }

    public static DeviceProtocolAdapterCodingExceptions genericReflectionError(Exception reflectionError, String className) {
        return new DeviceProtocolAdapterCodingExceptions(reflectionError, genericReflectionErrorExceptionCode(), className);
    }

    private static ExceptionCode genericReflectionErrorExceptionCode(){
        return new ExceptionCode(new CommonReferenceScope(), ExceptionType.CODING, CommonExceptionReferences.GENERIC_JAVA_REFLECTION_ERROR);
    }

    /**
     * Coding Exception created if some method is not supported for this class
     *
     * @param clazz      the {@link Class} where the coding error occurred
     * @param methodName the method name of the unsupported method
     * @return a newly created DeviceProtocolAdapterCodingExceptions
     */
    public static DeviceProtocolAdapterCodingExceptions unsupportedMethod(Class clazz, String methodName) {
        return new DeviceProtocolAdapterCodingExceptions(methodNotSupported(), clazz.getName(), methodName);
    }

    private static ExceptionCode methodNotSupported() {
        return new ExceptionCode(new CommonReferenceScope(), ExceptionType.CODING, CommonExceptionReferences.UNSUPPORTED_METHOD);
    }

}
