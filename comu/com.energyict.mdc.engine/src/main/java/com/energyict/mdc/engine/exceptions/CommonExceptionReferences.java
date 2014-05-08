package com.energyict.mdc.engine.exceptions;

import com.energyict.mdc.common.exceptions.ExceptionReference;

/**
 * Specifies the possible error references for all exceptions
 * defined in the monitoring components of the ComServer module.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-03 (13:44)
 */
public enum CommonExceptionReferences implements ExceptionReference<CommonReferenceScope> {

    MBEAN_OBJECT_FORMAT(100, 1),
    COMPOSITE_TYPE_CREATION(101, 1),
    COMPOSITE_DATA_CREATION(102, 1),
    UNKNOWN_COMPOSITE_DATA_ITEM(103, 2);

    public long toNumerical () {
        return code;
    }

    @Override
    public int expectedNumberOfArguments () {
        return this.expectedNumberOfArguments;
    }


    CommonExceptionReferences (long code, int expectedNumberOfArguments) {
        this.code = code;
        this.expectedNumberOfArguments = expectedNumberOfArguments;
    }

    private long code;
    private int expectedNumberOfArguments;

}