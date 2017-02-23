/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki;

import java.security.cert.X509Certificate;
import java.util.List;

/**
 * A trust store is a name collection of trusted certificates. The certificates are stored as TrustedCertificate.
 */
public interface TrustStore {

    long getId();

    /**
     * The name identifies the trust store. The name is a globally unique, human readable identifier.
     * @return trust store name.
     */
    public String getName();

    String getDescription();

    void setDescription(String description);

    /**
     * List of trusted certificates belonging to this trust store
     * @return List of certificates registered for this trust store
     */
    public List<TrustedCertificate> getCertificates();

    /**
     * Adds the provided certificate to the trust store. By adding a certificate to the trust store, that certificate
     * can be used to build a chain of trust.
     * @param x509Certificate to be trusted certificate, typically a Certifying Authority.
     */
    public TrustedCertificate addCertificate(X509Certificate x509Certificate);

    /**
     * Removes the provided certificate from the trust store.
     * @param x509Certificate to be trusted certificate
     */
    public void removeCertificate(X509Certificate x509Certificate);
}
