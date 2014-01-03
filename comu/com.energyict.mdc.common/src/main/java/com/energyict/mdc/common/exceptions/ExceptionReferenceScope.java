package com.energyict.mdc.common.exceptions;

import java.io.Serializable;

/**
 * Models the boundaries of where exceptions occur and at the same time,
 * provides a uniqueness context for these exceptions.
 * The idea is that {@link ExceptionReference}s are unique within an ExceptionReferenceScope.
 * <p>
 * Human readable descriptions of error messages are generated from resource bundles.
 * The ExceptionReferenceScope will provide the prefix that will be used
 * to produce the resource bundle message key.
 * </p>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-03-26 (17:25)
 */
public interface ExceptionReferenceScope extends Serializable {

    public String resourceKeyPrefix ();

}