package com.elster.jupiter.pki;

import com.elster.jupiter.domain.util.Finder;

import aQute.bnd.annotation.ProviderType;

/**
 * This is the main interface towards the PKI bundle. It provides access for the main PKI procedures.
 * Created by bvn on 1/26/17.
 */
@ProviderType
public interface PKIService {

    String COMPONENTNAME = "PKI";

    /**
     * Returns a list (through paged finder) for all existing keyTypes in Connexo
     * @return
     */
    Finder<KeyType> findAllKeyTypes();

    /**
     * Creates a new KeyType describing a symmetric key.
     * Required parameters are algorithm and depending on the algorithm: key size
     * @param name The name given to this key type. The name will be a unique identifier
     * @param keyAlgorithmName The algorithm for this key, e.g. AES, Blowfish, ...
     * @param keySize The size in bytes, e.g. 1024
     * @return The newly created, persisted KeyType
     */
    KeyType addSymmetricKeyType(String name, String keyAlgorithmName, int keySize);

    /**
     * Creates a new KeyType describing an asymmetric key.
     * Required parameters are algorithm (currently only RSA, DSA and EC are supported) and depending on the algorithm: curve or key size
     * @param name The name given to this key type. The name will be a unique identifier.
     * @return A builder guiding you through the creation of an asymmetric key definition
     */
    PKIService.AsyncBuilder addAsymmetricKeyType(String name);

    PKIService.AsyncBuilder addCertificateWithPrivateKeyType(String name); // TODO Fix
    KeyType addCertificateType(String name); // TODO fix

    public interface AsyncBuilder {
        AsyncKeySizeBuilder RSA();
        AsyncKeySizeBuilder DSA();
        AsyncCurveBuilder EC();
    };

    public interface AsyncKeySizeBuilder {
        AsyncKeySizeBuilder keySize(int keySize);
        KeyType add();
    }

    public interface AsyncCurveBuilder {
        AsyncCurveBuilder curve(String curveName);
        KeyType add();
    }

}
