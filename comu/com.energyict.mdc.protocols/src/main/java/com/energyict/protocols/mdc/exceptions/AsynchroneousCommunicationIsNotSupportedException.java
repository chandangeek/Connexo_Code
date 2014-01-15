package com.energyict.protocols.mdc.exceptions;

import com.energyict.mdc.common.exceptions.CommonExceptionReferences;
import com.energyict.mdc.common.exceptions.CommonReferenceScope;
import com.energyict.mdc.common.exceptions.ExceptionCode;
import com.energyict.mdc.common.exceptions.ExceptionType;
import com.energyict.mdc.protocol.api.exceptions.ComServerRuntimeException;

/**
 * Models the exceptional situations that occur when a developer has
 * forgotton or neglected to comply with coding standards
 * or constraints imposed by common framework components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-05-15 (09:02)
 */
public final class AsynchroneousCommunicationIsNotSupportedException extends ComServerRuntimeException {

    public AsynchroneousCommunicationIsNotSupportedException () {
        this(asynchroneousCommunicationIsNotSupportedCode());
    }

    private static ExceptionCode asynchroneousCommunicationIsNotSupportedCode () {
        return new ExceptionCode(new CommonReferenceScope(), ExceptionType.CODING, CommonExceptionReferences.ASYNCHRONEOUS_COMMUNICATION_IS_NOT_SUPPORTED);
    }

    private AsynchroneousCommunicationIsNotSupportedException(ExceptionCode code, Object... messageArguments) {
        super(code, messageArguments);
    }

    private AsynchroneousCommunicationIsNotSupportedException(Throwable cause, ExceptionCode code, Object... messageArguments) {
        super(cause, code, messageArguments);
    }

}