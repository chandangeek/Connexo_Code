/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.exception;

import com.energyict.mdc.common.ComServerRuntimeException;
import com.energyict.protocols.mdc.services.impl.MessageSeeds;

public class UnsupportedMethodException extends ComServerRuntimeException{

    public UnsupportedMethodException(Class clazz, String methodName) {
        super(MessageSeeds.UNSUPPORTED_METHOD, clazz,  methodName);
    }
}
