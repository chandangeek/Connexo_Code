package com.elster.jupiter.pki;

import aQute.bnd.annotation.ConsumerType;

import java.time.Instant;
import java.util.Optional;

/**
 * This class wraps an actual Key with the information required to read it from db or renew it.
 * Through offering PropertySpecs & properties, a generic interface is offered for the UI
 */
@ConsumerType
public interface SymmetricKeyWrapper extends HasDynamicPropertiesWithUpdatableValues, SecurityValueWrapper {

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

}
