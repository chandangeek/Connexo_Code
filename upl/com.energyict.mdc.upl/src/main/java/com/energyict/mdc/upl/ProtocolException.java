/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.upl;

import java.io.IOException;

/**
 * This exception is the common superclass of all com.energyict.protocol Exceptions.
 *
 * @author Karel
 */
public class ProtocolException extends IOException {

    public ProtocolException() {
        super();
    }

    public ProtocolException(String msg) {
        super(msg);
    }

    public ProtocolException(Exception e) {
        super(e);
    }

    public ProtocolException(Exception e, String msg) {
        super(msg, e);
    }

}