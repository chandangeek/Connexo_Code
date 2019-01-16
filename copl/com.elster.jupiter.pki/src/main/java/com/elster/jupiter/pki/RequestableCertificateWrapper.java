/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki;

import aQute.bnd.annotation.ProviderType;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;

import java.util.EnumSet;
import java.util.Optional;

/**
 * A RequestableCertificate differs in only one way from a normal certificate: It can contain a CSR
 * It might not always be possible for Connexo to generate a CSR, as the private key is required for this.
 * In the case of beacon of device certificates for example, only the beacon or device can generate a CSR
 * (usually on request of Connexo). Nevertheless, even in the those cases, Connexo will store the CSR generated by and
 * obtained from devices/beacon for export or some other purpose.
 */
@ProviderType
public interface RequestableCertificateWrapper extends CertificateWrapper {
    /**
     * If a CSR was created for this placeholder in order to obtain or renew a certificate, the CSR can be retrieved through this method.
     * We choose not to use the restricted class {@link sun.security.pkcs10.PKCS10} as return type for this method.
     *
     * @return PKCS10CertificationRequest if one was generated for this placeholder.
     */
    Optional<PKCS10CertificationRequest> getCSR();

    /**
     * Sets the CSR for this certificate wrapper. Most commonly this method will be called after having obtained a CSR from a device.
     * Nothing is done with the CSR, it is merely stored so that it can be exported when needed.
     *
     * @param csr The CSR to associate with this certificate wrapper.
     */
    void setCSR(PKCS10CertificationRequest csr, EnumSet<KeyUsage> keyUsages, EnumSet<ExtendedKeyUsage> extendedKeyUsages);

    /**
     * Sets the CSR for this certificate wrapper. Most commonly this method will be called after having obtained a CSR from a device.
     * Nothing is done with the CSR, it is merely stored so that it can be exported when needed.
     *
     * @param csr The encoded CSR to associate with this certificate wrapper.
     */
    void setCSR(byte [] encodedCsr, EnumSet<KeyUsage> keyUsages, EnumSet<ExtendedKeyUsage> extendedKeyUsages);

}