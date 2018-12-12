/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki;

import java.time.Instant;
import java.util.Optional;

/**
 * Interface bundles the interesting, common functionality on all Wrappers: certificate~, private key ~ and symmetric key ~
 */
public interface SecurityValueWrapper extends HasDynamicPropertiesWithUpdatableValues {
    /**
     * The expiration time is the {@link java.time.Instant} after which this entity expires.
     * @return {@link java.time.Instant} after which this entity is no longer valid, or Optional.empty() if expiration
     * is not applicable, e.g. if no key or certificate is present in the wrapper.
     */
    Optional<Instant> getExpirationTime();

    /**
     * Deletes this wrapper and the contained attributes and values
     */
    void delete();

    /**
     * @return if this security wrapper is valid (properties defined are set). Actually this method was extracted from AbstractDeviceSecurityAccessorImpl where it was looking inside each impl
     * and check if props are not set on null. I have my doubts about this method but at least with this approach (having it here) we can talk about encapsulation.
     */
    default boolean isValid() {
        return (!getProperties().containsValue(null) && getProperties().size()==getPropertySpecs().size());
    }

}
