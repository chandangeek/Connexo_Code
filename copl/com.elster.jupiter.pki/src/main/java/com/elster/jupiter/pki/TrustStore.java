/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki;

import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Optional;

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

    void setName(String name);

    void setDescription(String description);

    /**
     * List of trusted certificates belonging to this trust store
     * @return List of certificates registered for this trust store
     */
    public List<TrustedCertificate> getCertificates();

    /**
     * Adds the provided certificate to the trust store. By adding a certificate to the trust store, that certificate
     * can be used to build a chain of trust.
     * @param alias The alias for this certificate. The alias is unique within a truststore. Alias is case sensitive.
     * @param x509Certificate to be trusted certificate, typically a Certifying Authority.
     */
    public TrustedCertificate addCertificate(String alias, X509Certificate x509Certificate);

    /**
     * Removes the provided certificate from the trust store.
     * @param alias The alias of the certificate that is to be removed.
     */
    public void removeCertificate(String alias);

    /**
     * Find a trusted certificate by alias. Tha alias is unique withind the truststore and case-sensitive
     * @param alias The alias of the wanted certificate
     */
    public Optional<TrustedCertificate> findCertificate(String alias);


    long getVersion();

    /**
     * All {@link java.security.KeyStore.TrustedCertificateEntry} from the provided keystore will be added to this TrustStore
     * In case of alias conflict, the existing certificate will be overwritten.
     * @param keyStore The keystore to load {@link java.security.KeyStore.TrustedCertificateEntry} from.
     */
    void loadKeyStore(KeyStore keyStore);

    void save();

    /**
     * Removes the trust store and all trusted certificates contained in it.
     * A trust store can only be deleted if it is no longer referred to by a KeyAccessorType.
     */
    void delete();
}
