/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.services;

/**
 * Models the exceptional situation that occurs when a
 * {@link DeviceCacheMarshallingService} was asked to marschall
 * a cache object it does not recognize or manage.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-30 (15:37)
 */
public class NotAppropriateDeviceCacheMarshallingTargetException extends RuntimeException {

    public NotAppropriateDeviceCacheMarshallingTargetException() {
        super();
    }

}