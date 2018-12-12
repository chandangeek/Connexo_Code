/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.upl;

/**
 * This exception is thrown when the specified register is not available in the device.
 *
 * @author Karel
 */
public class NoSuchRegisterException extends ProtocolException {

    public NoSuchRegisterException() {
        super();
    }

    public NoSuchRegisterException(String msg) {
        super(msg);
    }

}