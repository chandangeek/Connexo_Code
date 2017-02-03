package com.energyict.mdc.upl.security;

import java.security.cert.Certificate;
import java.security.cert.CertificateException;

/**
 * Models a certificate alias as a String.
 * <p>
 * Copyrights EnergyICT
 * Date: 08/12/16
 * Time: 15:03
 */
public interface CertificateAlias {
    String getAlias();

    Certificate getCertificate() throws CertificateException;

    String getCertificateEncoded();
}