package com.energyict.dlms.protocolimplv2;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

/**
 * Adds additional functionality to the standard SecurityProvider.
 * Specifically, it supports the use of session keys that are used in general ciphering.
 * <p/>
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 21/01/2016 - 16:06
 */
public interface GeneralCipheringSecurityProvider extends SecurityProvider {

    /**
     * Return a generated key (either randomly or derived) that can be used to encrypt APDUs in a single application association.
     * This is similar to the use of the dedicated key.
     * However, the dedicated key is agreed in the AARQ, while the session key can be renewed for every APDU within an AA.
     */
    byte[] getSessionKey();

    /**
     * Update the cached session key with a new value.
     * This needs to be called when the used session key changes within an existing AA.
     */
    void setSessionKey(byte[] sessionKey);

    /**
     * The (static) public key of the server used for key agreement (ECDH).
     */
    X509Certificate getServerKeyAgreementCertificate();

    /**
     * The (static) public key of the server used for digital signature (ECDSA).
     */
    X509Certificate getServerSignatureCertificate();

    /**
     * The (static) certificate of the server used for digital signature (ECDSA).
     * It can be (optionally) present in the AARE and should also be known at our side (client side)
     */
    void setServerSignatureCertificate(X509Certificate serverSignatureCertificate);

    /**
     * The x.509 v3 certificate that matches our (client) private signing key.
     * It can be (optionally) present in the AARQ, or can be imported into the server device (meter)
     */
    X509Certificate getClientSigningCertificate();

    /**
     * The client's (our) private key used for digital signing (ECDSA)
     */
    PrivateKey getClientPrivateSigningKey();

    /**
     * The client's (our) private key used key agreement (ECDH)
     */
    PrivateKey getClientPrivateKeyAgreementKey();
}