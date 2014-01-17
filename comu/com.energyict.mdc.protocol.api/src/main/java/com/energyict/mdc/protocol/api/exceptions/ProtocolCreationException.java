package com.energyict.mdc.protocol.api.exceptions;

import com.energyict.mdc.common.exceptions.CommonExceptionReferences;
import com.energyict.mdc.common.exceptions.CommonReferenceScope;
import com.energyict.mdc.common.exceptions.ExceptionCode;
import com.energyict.mdc.common.exceptions.ExceptionType;

/**
 * Models the exceptional situation that occurs when a protocol
 * could not be created because of some java reflection layer problem.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-24 (15:27)
 */
public class ProtocolCreationException extends ComServerRuntimeException {

    public ProtocolCreationException(String javaClassName) {
        super(genericReflectionErrorExceptionCode(), javaClassName);
    }

    public ProtocolCreationException(Class unsupportedLegacyClass) {
        super(unsupportedLegacyClassExceptionCode(), unsupportedLegacyClass.getName());
    }

    private static ExceptionCode genericReflectionErrorExceptionCode(){
        return new ExceptionCode(new CommonReferenceScope(), ExceptionType.CODING, CommonExceptionReferences.GENERIC_JAVA_REFLECTION_ERROR);
    }

    private static ExceptionCode unsupportedLegacyClassExceptionCode (){
        return new ExceptionCode(new CommonReferenceScope(), ExceptionType.CODING, CommonExceptionReferences.UNSUPPORTED_LEGACY_PROTOCOL_TYPE);
    }

}