package com.elster.jupiter.pki;

/**
 * KeyType identifies the properties of a CryptoEntity (certificate/ keypair/ symmetric key) such as algorithm, bit length, ...
 * For an symmetric key, the following properties apply:
 * - Algorithm
 * - Key size
 * For an asymmetric key, the following properties apply:
 * - rsa : key size
 * - dsa : key size
 * - ec: curve name
 *
 */
public interface KeyType {

    /**
     * The name for a KeyType is intended to allow easy identification of a type. It does not need to match on other value.
     * The name needs to be unique
     * @return The KeyType name
     */
    String getName();

    void setName(String name);

    /**
     * The cryptographic type identifies the actual stored value. This can be a X509Certificate with or without private
     * key, or a symmetric key.
     * @return
     */
    CryptographicType getCryptographicType();

    /**
     * Identifier of the algorithm to generate a new value or reconstruct current value, e.g. for a symmetric key: "AES/CBC/PKCS5Padding/128", or
     * for an async keypair: "EC", "DSA", "RSA"
     * for a sym key: "AES", "Blowfish", ...
     * @return
     */
    String getAlgorithm();

    /**
     * Returns the bit length in case of a symmetric key
     * e.g. for an RSA key: 1024
     * @return
     */
    Integer getKeySize();

    /**
     * Returns the curve name in case of an EC key
     * e.g. "prime192v1"
     * @return
     */
    String getCurve();
}
