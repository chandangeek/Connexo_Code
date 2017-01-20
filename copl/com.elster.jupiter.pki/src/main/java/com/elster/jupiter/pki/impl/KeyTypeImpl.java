package com.elster.jupiter.pki.impl;

import com.elster.jupiter.pki.CryptographicType;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.util.Checks;

import java.security.Provider;
import java.security.Security;
import java.util.Optional;

/**
 * Created by bvn on 1/18/17.
 */
public class KeyTypeImpl implements KeyType {
    private long id;
    private String name;
    private CryptographicType cryptographicType;
    private String keyAlgorithm;
    private String provider;

    public long getId() {
        return id;
    }

    /**
     * The name for a KeyType is intended to allow easy identification of a type. It does not need to match on other value.
     * The name needs to be unique
     * @return The KeyType name
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The cryptographic type identifies the actual stored value. This can be a X509Certificate with or without private
     * key, or a symmetric key.
     * @return
     */
    public CryptographicType getCryptographicType() {
        return cryptographicType;
    }

    /**
     * Identifier of the algorithm to generate a new value or reconstruct current value, e.g. for a symmetric key: "AES/CBC/PKCS5Padding/128", or
     * for an EC keypair: "EC/prime192v1/ECDSA"
     *
     * @return
     */
    public String getKeyAlgorithm() {
        return keyAlgorithm;
    }

    public void setKeyAlgorithm(String keyAlgorithm) {
        this.keyAlgorithm = keyAlgorithm;
    }

    /**
     * The provider of the cryptographic algorithm, by default Connexo will install providers BouncyCastle and java default providers.
     * If no provider is entered, no explicit provider will be used for manipulation of the contained cryptographic value.
     */
    public Optional<Provider> getProvider() {
        return Checks.is(provider).emptyOrOnlyWhiteSpace()?Optional.empty():Optional.of(Security.getProvider(provider));
    }

    public void setProvider(Provider provider) {
        this.provider = provider.getName();
    }
}
