package com.energyict.mdc.common.exceptions;

import com.energyict.mdc.common.exceptions.ExceptionReferenceScope;

/**
 * Models the scope that contains the the common commponents of the ComServer module.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-05-15 (09:02)
 */
public final class CommonReferenceScope implements ExceptionReferenceScope {

    @Override
    public String resourceKeyPrefix () {
        return "CSC";
    }

}