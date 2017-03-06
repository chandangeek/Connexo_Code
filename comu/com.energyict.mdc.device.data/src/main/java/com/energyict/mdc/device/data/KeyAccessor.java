/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.energyict.mdc.device.data;

import com.elster.jupiter.pki.KeyAccessorType;

import java.util.Optional;

/**
 * Represents access to a wrapper object. This security object can be a certificate, symmetric key or password.
 * The Security accessor stores the actual value for a KeyAccessorType
 * An accessor stores two values: one for current use, and one value that is stored during the renew process.
 */
public interface KeyAccessor<T> {

    /**
     * Get the device this KeyAccessor holds a value for
     */
    Device getDevice();

    /**
     * Get the KeyAccessorType this value belongs to
     */
    KeyAccessorType getKeyAccessorType();

    /**
     * The actual value is the value to be used at present
     * @return The current value
     */
    T getActualValue();

    /**
     * Sets a new current value.
     * @param newWrapperValue The new wrapper to use as current value
     */
    void setActualValue(T newWrapperValue);

    /**
     * Whenever this security element is in the process of being renewed. The future value is saved in the temp field.
     * Casual users should not need to access this value, it is stored for the renew process.
     * @return The value as generated for the renew process.
     */
    Optional<T> getTempValue();

    /**
     * Create a new value for the KeyAccessor. The new value will be stored in the temp value holder.
     * If there is already a temp value, it will be overwritten!
     */
//    void renew; // TODO implement, will delegate to wrapper who (should) implement renewable

    /**
     * Allows for the key accessor to be updated only
     */
    void save();
}
