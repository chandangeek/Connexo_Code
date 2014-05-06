package com.energyict.mdc.engine.impl.commands.store.exceptions;

import com.energyict.mdc.common.BusinessException;

/**
* Copyrights EnergyICT
* Date: 15/01/14
* Time: 15:14
*/
public class RuntimeBusinessException extends RuntimeException {

    public RuntimeBusinessException(BusinessException cause) {
        super(cause);
    }

    @Override
    public synchronized BusinessException getCause() {
        return (BusinessException) super.getCause();
    }
}
