package com.elster.jupiter.pki;

import java.time.Instant;

/**
 * This class wraps an actual Key with the information required to read it from db or renew it.
 * Through offering PropertySpecs & properties, a generic interface is offered for the UI
 */
public interface SymmetricKeyWrapper extends HasDynamicPropertiesWithUpdatableValues, Renewable {

    /**
     * Defines the method used to store keys by this implementation.
     * @return
     */
    String getKeyEncryptionMethod();

    /**
     * The exact date when the value of this element will expire. The value should be renewed by thia date.
     * @return date until which this element is valid
     */
    Instant getExpirationTime();
}
