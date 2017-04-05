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
     * The expiration time is the {@link java.time.Instant) after which this entity expires.
     * @return {@link java.time.Instant) after which this entity is no longer valid, or Optional.empty() if expiration
     * is not applicable, e.g. if no key or certificate is present in the wrapper.
     */
    Optional<Instant> getExpirationTime();
}
