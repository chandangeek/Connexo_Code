package com.energyict.mdc.engine.exceptions;

import com.energyict.mdc.common.exceptions.ExceptionReference;

/**
 * Specifies the possible error references for all exceptions that
 * can occur in the ComServer execution module.
 *
 * @author gna
 * @since 28/03/12 - 15:02
 */
public enum ComServerExecutionExceptionReferences implements ExceptionReference<ComServerExecutionReferenceScope> {

    CONNECTION_TIMEOUT(200, 1),

    //SECURITY(300, 0),

    /**
     * The newly added ComCommand is not unique in the root
     */
    COMMAND_NOT_UNIQUE(500, 1),
    /**
     * Identifying that the current command may not be executed
     */
    ILLEGAL_COMMAND(501, 2);

    private final long code;
    private final int expectedNumberOfArguments;

    private ComServerExecutionExceptionReferences(final long code, final int expectedNumberOfArguments) {
        this.code = code;
        this.expectedNumberOfArguments = expectedNumberOfArguments;
    }

    /**
     * Converts this ExceptionReference to its numerial representation.
     *
     * @return The numerical representation
     */
    @Override
    public long toNumerical() {
        return code;
    }

    /**
     * Returns the number of arguments that are expected for
     * the human readable description of the problem.
     *
     * @return The number of arguments
     */
    @Override
    public int expectedNumberOfArguments() {
        return expectedNumberOfArguments;
    }
}
