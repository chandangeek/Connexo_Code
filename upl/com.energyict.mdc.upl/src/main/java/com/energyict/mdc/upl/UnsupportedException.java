/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.upl;

/**
 * Thrown when an optional operation is not support by the specific protocol.
 *
 * @author Karel
 */
public class UnsupportedException extends ProtocolException {

    public UnsupportedException() {
        super();
    }

    public UnsupportedException(String msg) {
        super(msg);
    }

}