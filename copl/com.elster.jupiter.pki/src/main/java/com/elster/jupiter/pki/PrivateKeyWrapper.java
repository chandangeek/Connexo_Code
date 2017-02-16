/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki;

import aQute.bnd.annotation.ConsumerType;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.util.Map;

/**
 *
 * Created by bvn on 1/12/17.
 */
@ConsumerType
public interface PrivateKeyWrapper extends HasDynamicPropertiesWithUpdatableValues {

    /**
     * Defines the method used to store keys by this implementation.
     * @return
     */
    String getKeyEncryptionMethod();

    /**
     * Creates an empty instance of PrivateKey that can be filled in by the caller. How the value for the PrivateKey is determined, depends
     * on the KeyEncryptionType
     * @return The PrivateKey build from the provided properties. Note that java.security.PrivateKey is provided through
     * the Java's SPI.
     * @throws InvalidKeyException
     */
    PrivateKey getPrivateKey() throws InvalidKeyException;

    /**
     * Generate a new random value for this entity.
     * @return The newly generated value.
     * @throws NoSuchAlgorithmException
     */
    PrivateKeyWrapper renewValue() throws
            NoSuchAlgorithmException,
            InvalidAlgorithmParameterException,
            NoSuchProviderException;

    /**
     * These properties are defined by the implementor. In case of a plaintext key, there will be a property containing
     * the actual bytes of the private key. In case of a IrreversibleHsmPrivateKey, the property will be the private key
     * label (or maybe more, depending on the hsm interface?), in case of an IrreversibleDbPrivateKey, the properties will
     * be the encoded private key, the label of the KEK key and the IV.
     * @param properties The properties and the values to set. Unknown properties will be ignored without warning.
     */
    void setProperties(Map<String, Object> properties);
}
