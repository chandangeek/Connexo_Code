package com.energyict.mdc.engine.exceptions;

import com.energyict.mdc.common.exceptions.ExceptionReferenceScope;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-03-26 (17:38)
 */
public final class ComServerPersistenceReferenceScope implements ExceptionReferenceScope {

    public static final ComServerPersistenceReferenceScope SINGLETON = new ComServerPersistenceReferenceScope();

    @Override
    public String resourceKeyPrefix () {
        return "CSP";
    }

    private ComServerPersistenceReferenceScope () {
        super();
    }

}