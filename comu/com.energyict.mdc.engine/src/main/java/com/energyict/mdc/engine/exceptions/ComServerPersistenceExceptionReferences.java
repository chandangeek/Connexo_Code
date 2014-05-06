package com.energyict.mdc.engine.exceptions;

import com.energyict.mdc.common.exceptions.ExceptionReference;

/**
 * Specifies the possible error references for all exceptions that
 * can occur in the ComServer persistence module.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-03-26 (17:39)
 */
public enum ComServerPersistenceExceptionReferences implements ExceptionReference<ComServerPersistenceReferenceScope> {

    UNEXPECTED_SQL_ERROR (100, 1);

    public long toNumerical () {
        return code;
    }

    @Override
    public int expectedNumberOfArguments () {
        return this.expectedNumberOfArguments;
    }


    ComServerPersistenceExceptionReferences (long code, int expectedNumberOfArguments) {
        this.code = code;
        this.expectedNumberOfArguments = expectedNumberOfArguments;
    }

    private long code;
    private int expectedNumberOfArguments;

}