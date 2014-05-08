package com.energyict.mdc.engine.exceptions;

import com.energyict.mdc.common.exceptions.ExceptionReferenceScope;

/**
 * Models the scope that contains the the monitoring commponents of the ComServer module.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-03 (13:47)
 */
public final class CommonReferenceScope implements ExceptionReferenceScope {

    @Override
    public String resourceKeyPrefix () {
        return "CSMON";
    }

}