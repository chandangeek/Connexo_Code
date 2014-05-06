package com.energyict.mdc.engine.exceptions;

import com.energyict.mdc.common.exceptions.ExceptionReferenceScope;

/**
 * Models the scope represented by the execution of the ComServer model.
 *
 * @author gna
 * @since 28/03/12 - 14:50
 */
public final class ComServerExecutionReferenceScope implements ExceptionReferenceScope {

    public static final ComServerExecutionReferenceScope SINGLETON = new ComServerExecutionReferenceScope();

    private ComServerExecutionReferenceScope(){
        super();
    }

    @Override
    public String resourceKeyPrefix() {
        return "CSE";
    }

}
