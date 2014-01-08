package com.energyict.mdc.common.exceptions;

/**
 * Models a partial unique identification of an exceptional situation
 * that has occurred in the ComServer.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-03-26 (16:43)
 */
public interface ExceptionReference<M extends ExceptionReferenceScope> {

    /**
     * Converts this ExceptionReference to its numerial representation.
     *
     * @return The numerical representation
     */
    public long toNumerical ();

    /**
     * Returns the number of arguments that are expected for
     * the human readable description of the problem.
     *
     * @return The number of arguments
     */
    public int expectedNumberOfArguments ();

}