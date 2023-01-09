/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Represents access to a wrapper object. This security object can be a certificate only (for now).
 * The Security accessor stores the real value(s) for a KeyAccessorType
 * An accessor stores two values: one for current use (ActualValue), and one value that is stored during the renew
 * process (TempValue).
 */
@ProviderType
public interface SecurityAccessor<T extends SecurityValueWrapper> extends HasName, HasId {

    /**
     * Get the KeyAccessorType this value belongs to
     */
    SecurityAccessorType getSecurityAccessorType();

    /**
     * Returns the name of the security accessor.
     */
    @Override
    default String getName() {
        return getSecurityAccessorType().getName();
    }

    /**
     * Returns the id of the security accessor.
     */
    @Override
    default long getId() {
        return getSecurityAccessorType().getId();
    }

    /**
     * The actual value is the value to be used at present. A KeyAccessor could also exist with only a tempValue, without
     * actual.
     * @return The current value
     */
    Optional<T> getActualValue();

    /**
     * Sets a new current value.
     * @param newValueWrapper The new wrapper to use as current value
     */
    void setActualValue(T newValueWrapper);

    /**
     * Whenever this security element is in the process of being renewed. The future value is saved in the temp field.
     * Casual users should not need to access this value, it is stored for the renew process.
     * @return The value as generated for the renew process.
     */
    Optional<T> getTempValue();

    /**
     * Sets the temp value on this KeyAccessor. Existing tempValue will be overwritten.
     * @param newValueWrapper The value to set as temp value.
     */
    void setTempValue(T newValueWrapper);

    /**
     * Create a new value for the KeyAccessor. The new value will be stored in the temp value holder.
     * If there is already a temp value, it will be overwritten!
     */
    void renew();

    /**
     * Swaps the actual and temp value. This method can only be called of the Accessor has both an actual an a temp value.
     */
    void swapValues();

    /**
     * The temp value and all keys and/or certificates it contains is destroyed. The accessor is no longer considered
     * tobe in state 'swapped'
     */
    void clearTempValue();

    /**
     * The actual value and all keys and/or certificates it contains is destroyed. This method does not affect the
     * 'swapped' state of the accessor
     */
    void clearActualValue();

    /**
     * Allows for the security accessor to be updated only
     */
    void save();

    /**
     * Even without an actual value, the KeyAccessor can provide the expected PropertySpecs
     * @return PropertySpecs as reported by the wrapper.
     */
    List<PropertySpec> getPropertySpecs();

    /**
     * Deletes this KeyAccessor and actual and temp value, if present.
     * If deletion of actual and temp values fail (veto exception), they will be left as they are, but the KeyAccessor
     * will be deleted regardless
     */
    void delete();

    /**
     * Returns the current version of this KeyAccessor. Version is used to prevent concurrent modification.
     * @return version of this object
     */
    long getVersion();

    /**
     * Indicates if the values are in their original place or swapped. After clearing the temp value of a KeyAccessor,
     * the actual value is always considered in his original place.
     * @return true if swapped, else the values is in its original place
     */
    boolean isSwapped();

    /**
     * Returns the Instant of this KeyAccessor's latest modification. This information is part of the general audit-information
     * @return Time of latest modification
     */
    Instant getModTime();
}
