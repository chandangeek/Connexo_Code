package com.energyict.mdc.engine.exceptions;

import com.energyict.mdc.common.exceptions.ExceptionReferenceScope;

/**
 * Models the scope represented by the implementation of the ComServer model.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-03-26 (17:38)
 */
public final class ComServerModelReferenceScope implements ExceptionReferenceScope {

    @Override
    public String resourceKeyPrefix () {
        return "CSM";
    }

}