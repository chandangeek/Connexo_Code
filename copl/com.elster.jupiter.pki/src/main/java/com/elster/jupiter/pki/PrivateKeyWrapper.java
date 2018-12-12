/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki;

import aQute.bnd.annotation.ConsumerType;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;

import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

/**
 * This class wraps an actual PrivateKey with the information required to read it from db or renew it.
 * Through offering PropertySpecs & properties, a generic interface is offered for the UI
 */
@ConsumerType
public interface PrivateKeyWrapper extends HasDynamicPropertiesWithUpdatableValues, SecurityValueWrapper {

    /**
     * The exact date when the value of this element will expire. The value should be renewed by this date.
     *
     * @return date until which this element is valid
     */
    Optional<Instant> getExpirationTime();

    /**
     * Defines the method used to store keys by this implementation.
     */
    String getKeyEncryptionMethod();

    /**
     * Returns the PrivateKey held by this element. How the value for the PrivateKey is determined, depends
     * on the KeyEncryptionType.
     *
     * @return The PrivateKey build from the provided properties. Note that java.security.PrivateKey is provided through
     * the Java's SPI.
     * @throws InvalidKeyException
     */
    Optional<PrivateKey> getPrivateKey() throws InvalidKeyException;

    /**
     * These properties are defined by the implementor. In case of a plaintext key, there will be a property containing
     * the actual bytes of the private key. In case of a IrreversibleHsmPrivateKey, the property will be the private key
     * label (or maybe more, depending on the hsm interface?), in case of an IrreversibleDbPrivateKey, the properties will
     * be the encoded private key, the label of the KEK key and the IV.
     *
     * @param properties The properties and the values to set. Unknown properties will be ignored without warning.
     */
    void setProperties(Map<String, Object> properties);

    /**
     * Generates a CSR with provided distinguished name.
     *
     * @param subjectDN The X500Name to be used as subject DN
     */
    PKCS10CertificationRequest generateCSR(X500Name subjectDN, String signatureAlgorithm); // TODO drop signatureAlgorithm

    /**
     * Deletes the private key wrapper and all information contained within the wrapper
     */
    void delete();

    /**
     * Allows the generation of a random value for an empty wrapper, in this case, a private key
     * It's up to the implementing class to make sure all renewal information is available (through linking
     * KeyTypes/KeyAccessorTypes)
     * Note that not all key encryption methods will permit automatic renewal.
     * @return returns the public key associated with the generated private key
     */
    PublicKey generateValue();

    /**
     * Get the KeyType describing the private key.
     * The KeyType will describe the key algorithm.
     * @return
     */
    KeyType getKeyType();

}
