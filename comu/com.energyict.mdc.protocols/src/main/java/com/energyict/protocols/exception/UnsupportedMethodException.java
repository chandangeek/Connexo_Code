package com.energyict.protocols.exception;

import com.energyict.mdc.common.ComServerRuntimeException;
import com.energyict.protocols.mdc.services.impl.MessageSeeds;

/**
 * Copyrights EnergyICT
 * Date: 12/4/14
 * Time: 10:10 AM
 */
public class UnsupportedMethodException extends ComServerRuntimeException{

    public UnsupportedMethodException(Class clazz, String methodName) {
        super(MessageSeeds.UNSUPPORTED_METHOD, clazz,  methodName);
    }
}
