/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.energyict.mdc.device.data;

import com.elster.jupiter.pki.SecurityValueWrapper;

import aQute.bnd.annotation.ProviderType;

/**
 * Represents access to a wrapper object on device level. This security object can be a certificate, symmetric key or password.
 * The Security accessor stores the real value(s) for a KeyAccessorType
 * An accessor stores two values: one for current use (ActualValue), and one value that is stored during the renew
 * process (TempValue).
 */
@ProviderType
public interface SecurityAccessor<T extends SecurityValueWrapper> extends com.elster.jupiter.pki.SecurityAccessor<T> {

    /**
     * Get the device this KeyAccessor holds a value for
     */
    Device getDevice();

    /**
     * Status indicates if this KeyAccessor is ready for use or not. A KeyAccessor is considered complete/ready for use
     * if it has an actual value and all properties of this actual value have been filled id
     * @return Complete if ready for use, Incomplete otherwise.
     */
    KeyAccessorStatus getStatus();
}
