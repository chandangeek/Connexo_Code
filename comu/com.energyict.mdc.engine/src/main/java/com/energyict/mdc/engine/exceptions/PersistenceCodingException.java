package com.energyict.mdc.engine.exceptions;

import com.energyict.mdc.protocol.api.exceptions.ComServerRuntimeException;
import com.energyict.mdc.common.exceptions.ExceptionCode;
import com.energyict.mdc.common.exceptions.ExceptionType;

import java.sql.SQLException;

/**
 * Models the exceptional situations that occur when a developer has
 * forgotten or neglected to provide code required by the EIServer persistence framework.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-03-28 (15:48)
 */
public final class PersistenceCodingException extends ComServerRuntimeException {

    public static PersistenceCodingException unrecognizedDiscriminator (int discriminator, Class clazz) {
        return new PersistenceCodingException(unrecognizedDiscriminatorExceptionCode(), discriminator,  clazz.getName());
    }

    private static ExceptionCode unrecognizedDiscriminatorExceptionCode () {
        return new ExceptionCode(new ComServerModelReferenceScope(), ExceptionType.CODING, ComServerModelExceptionReferences.UNRECOGNIZED_DISCRIMINATOR);
    }

    public static PersistenceCodingException unexpectedSqlError (SQLException e) {
        return new PersistenceCodingException(e, unexpectedSqlError());
    }

    private static ExceptionCode unexpectedSqlError () {
        return new ExceptionCode(new ComServerModelReferenceScope(), ExceptionType.CODING, ComServerModelExceptionReferences.SQL_ERROR);
    }

    private PersistenceCodingException (ExceptionCode code, Object... messageArguments) {
        super(code, messageArguments);
    }

    private PersistenceCodingException (Throwable cause, ExceptionCode code, Object... messageArguments) {
        super(cause, code, messageArguments);
    }

}