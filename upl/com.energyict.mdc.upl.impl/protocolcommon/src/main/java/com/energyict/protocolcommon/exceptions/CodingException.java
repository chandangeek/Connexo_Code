/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolcommon.exceptions;

import com.energyict.protocol.exception.ProtocolExceptionMessageSeeds;

/**
 * Provides functionality to generate exceptions based on the parsing of some data
 *
 * @author gna
 * @since 28/03/12 - 15:12
 */
public class CodingException extends com.energyict.protocol.exceptions.CodingException {

    protected CodingException(Throwable cause, ProtocolExceptionMessageSeeds code, Object... messageArguments) {
        super(cause, code, messageArguments);
    }

    protected CodingException(ProtocolExceptionMessageSeeds reference, Object... messageArguments) {
        super(reference, messageArguments);
    }

    private CodingException(ProtocolExceptionMessageSeeds reference, Exception cause) {
        super(cause, reference, cause.getMessage());
    }

    public static CodingException genericReflectionError(Exception reflectionError, Class someClass) {
        return genericReflectionError(reflectionError, someClass.getName());
    }

    public static CodingException genericReflectionError(Exception reflectionError, String className) {
        return new CodingException(reflectionError, ProtocolExceptionMessageSeeds.GENERIC_JAVA_REFLECTION_ERROR, className);
    }

    public static CodingException protocolImplementationError(String error) {
        return new CodingException(ProtocolExceptionMessageSeeds.PROTOCOL_IMPLEMENTATION_ERROR, error);
    }

    /**
     * Coding Exception created if some method is not supported for this class
     *
     * @param clazz      the {@link Class} where the coding error occurred
     * @param methodName the method name of the unsupported method
     * @return a newly created CodingException
     */
    public static CodingException unsupportedMethod(Class clazz, String methodName) {
        return new CodingException(ProtocolExceptionMessageSeeds.UNSUPPORTED_METHOD, clazz.getName(), methodName);
    }

    /**
     * Constructs a new CodingException to represent a failure
     * to recognize an enum value, most likely a switch branch that is missing in the code.
     *
     * @param enumValue The unrecognized enum value
     * @return The CodingException
     */
    public static <T extends Enum> CodingException unrecognizedEnumValue (T enumValue) {
        return new CodingException(ProtocolExceptionMessageSeeds.UNRECOGNIZED_ENUM_VALUE, enumValue.getClass(), enumValue.ordinal());
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
        return new CodingException(ProtocolExceptionMessageSeeds.UNRECOGNIZED_ENUM_VALUE, enumClass.getName(), ordinalValue);
    }
}