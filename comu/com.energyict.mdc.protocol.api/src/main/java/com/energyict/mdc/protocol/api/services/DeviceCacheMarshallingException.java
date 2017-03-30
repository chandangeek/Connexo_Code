/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.services;

/**
 * Models the exceptional situation that occurs when a failure
 * occurs in a {@link DeviceCacheMarshallingService}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-30 (15:37)
 */
public class DeviceCacheMarshallingException extends RuntimeException {

    public DeviceCacheMarshallingException(String message, Throwable cause) {
        super(message, cause);
    }

}