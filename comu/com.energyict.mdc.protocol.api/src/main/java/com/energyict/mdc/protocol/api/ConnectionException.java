/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api;

import java.io.IOException;

/**
 * Models the exceptional situation that occurs when a connection
 * with a device could not be established.
 * There will always be a nested exception that
 * provides details of the failure.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-06-15 (12:10)
 */
public class ConnectionException extends IOException {

    public ConnectionException (String message, Throwable cause) {
        super(message, cause);
    }

    public ConnectionException (Throwable cause) {
        super(cause);
    }

}