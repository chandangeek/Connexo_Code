package com.energyict.mdc.upl.security;

import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

/**
 * Models a private key alias as a String
 *
 * Copyrights EnergyICT
 * Date: 08/12/16
 * Time: 14:34
 */
public interface PrivateKeyAlias {

    String getAlias();

    Certificate getCertificate() throws CertificateException;

    PrivateKey getPrivateKey() throws InvalidKeySpecException;

}