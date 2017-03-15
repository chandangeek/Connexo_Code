/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.exception;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;

public class GenericException extends RuntimeException {

    public GenericException() {
        super();
    }

    public GenericException(String message) {
        super(message);
    }

    public GenericException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getErrorCode() {
        return getHostname() + "-" + Long.toHexString(System.currentTimeMillis()).toUpperCase();
    }

    private String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "UNKOWNHOST";
        }
    }

}
