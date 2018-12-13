package com.elster.jupiter.pki;

import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;

import java.util.EnumSet;

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
public interface KeyType extends HasName, HasId {

    /**
     * The name for a KeyType is intended to allow easy identification of a type. It does not need to match on other value.
     * The name needs to be unique.
     * @return The KeyType name
     */
    String getName();

    /**
     * The name for a KeyType is intended to allow easy identification of a type. It does not need to match on other value.
     * The name needs to be unique.
     */
    void setName(String name);

    String getDescription();

    void setDescription(String description);

    /**
     * The cryptographic type identifies the actual stored value. This can be a X509Certificate with or without private
     * key, or a symmetric key.
     * @return The {@link CryptographicType}
     */
    CryptographicType getCryptographicType();

    /**
     * Identifier of the algorithm to generate a new value or reconstruct current value
     * for an asymmetric key pair: "EC", "DSA", "RSA"
     * for a symmetric key: "AES", "Blowfish", ...
     * @return The key algorithm, if applicable.
     */
    String getKeyAlgorithm();

    /**
     * Identifier of the signing algorithm on certificates
     * e.g. SHA256withRSA
     * @return The signing algorithm if applicable, non-client certificate types will return null;
     */
    String getSignatureAlgorithm();

    /**
     * Returns the bit length in case of a symmetric key
     * e.g. for an RSA key: 1024
     * @return
     */
    Integer getKeySize();

    EnumSet<KeyUsage> getKeyUsages();

    /**
     * Returns the currently defined key usage as a BouncyCastle {@link org.bouncycastle.asn1.x509.KeyUsage}
     * @return org.bouncycastle.asn1.x509.KeyUsage
     */
    org.bouncycastle.asn1.x509.KeyUsage getKeyUsage();

    EnumSet<ExtendedKeyUsage> getExtendedKeyUsages();

    /**
     * Returns the currently defined extended key usages as a BC {@link ExtendedKeyUsage}
     * @return ExtendedKeyUsage
     */
    org.bouncycastle.asn1.x509.ExtendedKeyUsage getExtendedKeyUsage();

    /**
     * Returns the curve name in case of an EC key
     * e.g. "prime192v1"
     * @return
     */
    String getCurve();

    /**
     * Returns the length in characters this password will have
     * @return
     */
    Integer getPasswordLength();

    /**
     * Specify use of lower case characters
     * @return true is lower case characters can be used, false otherwise
     */
    Boolean useLowerCaseCharacters();

    /**
     * Specify use of upper case characters
     * @return true is upper case characters can be used, false otherwise
     */
    Boolean useUpperCaseCharacters();

    /**
     * Specify use of numbers
     * @return true is numbers can be used, false otherwise
     */
    Boolean useNumbers();

    /**
     * Specify use of special characters
     * @return true is special characters can be used, false otherwise
     */
    Boolean useSpecialCharacters();
}
