/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki;

import java.time.Instant;
import java.util.Optional;

/**
 * This class wraps an actual passphrase with the information required to read it from db or renew it.
 * Through the offering of PropertySpecs & properties, a generic interface is offered for the UI
 */
public interface PassphraseWrapper extends HasDynamicPropertiesWithUpdatableValues, SecurityValueWrapper {

    /**
     * Defines the method used to store keys by this implementation.
     * @return
     */
    String getKeyEncryptionMethod();

    /**
     * The exact date when the value of this element will expire. The value should be renewed by this date.
     * @return date until which this element is valid
     */
    Optional<Instant> getExpirationTime();

    /**
     * Deletes this wrapper and the key it contains
     */
    void delete();

    /**
     * Allows the generation of a random value for an empty wrapper, in this case, a private key
     * It's up to the implementing class to make sure all renewal information is available (through linking
     * KeyTypes/KeyAccessorTypes)
     * Note that not all key encryption methods might permit automatic renewal.
     * @param keyAccessorType Contains values required by renew process // TODO could also provide individual values, better?
     */
    public void generateValue(KeyAccessorType keyAccessorType);

}
