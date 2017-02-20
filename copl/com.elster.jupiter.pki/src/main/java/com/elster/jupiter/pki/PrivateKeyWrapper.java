/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki;

import aQute.bnd.annotation.ConsumerType;

import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.time.Instant;
import java.util.Map;

/**
 * This class wraps an actual PrivateKey with the information required to read it from db or renew it.
 * Through offering PropertySpecs & properties, a generic interface is offered for the UI
 */
@ConsumerType
public interface PrivateKeyWrapper extends HasDynamicPropertiesWithUpdatableValues {

    /**
     * The exact date when the value of this element will expire. The value should be renewed by thia date.
     * @return date until which this element is valid
     */
    Instant getExpirationTime();

    /**
     * Defines the method used to store keys by this implementation.
     * @return
     */
    String getKeyEncryptionMethod();

    /**
     * Returns the PrivateKey held by this element. How the value for the PrivateKey is determined, depends
     * on the KeyEncryptionType
     * @return The PrivateKey build from the provided properties. Note that java.security.PrivateKey is provided through
     * the Java's SPI.
     * @throws InvalidKeyException
     */
    PrivateKey getPrivateKey() throws InvalidKeyException;

    /**
     * Generate a new random value for this entity.
     * @throws InvalidKeyException
     */
    void renewValue() throws InvalidKeyException;

    /**
     * These properties are defined by the implementor. In case of a plaintext key, there will be a property containing
     * the actual bytes of the private key. In case of a IrreversibleHsmPrivateKey, the property will be the private key
     * label (or maybe more, depending on the hsm interface?), in case of an IrreversibleDbPrivateKey, the properties will
     * be the encoded private key, the label of the KEK key and the IV.
     * @param properties The properties and the values to set. Unknown properties will be ignored without warning.
     */
    void setProperties(Map<String, Object> properties);
}
