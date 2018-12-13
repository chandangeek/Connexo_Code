/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.upl.properties;

import com.energyict.mdc.upl.ProtocolException;

/**
 * Thrown from a {@link PropertySpec} when a value is not valid.
 *
 * @author Rudi Vankeirsbilck (rvk)
 * @since 2016-10-28 (15:09)
 */
public abstract class PropertyValidationException extends ProtocolException {

    public PropertyValidationException() {
        super();
    }

    public PropertyValidationException(String msg) {
        super(msg);
    }

    public PropertyValidationException(Exception e) {
        super(e);
    }

    public PropertyValidationException(Exception e, String msg) {
        super(e, msg);
    }

}