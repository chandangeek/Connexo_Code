package com.elster.jupiter.pki;

import com.elster.jupiter.util.HasId;

import aQute.bnd.annotation.ProviderType;

import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Optional;

/**
 * This object represents a Certificate stored in Connexo. A Certificate can optionally linked to
 * - a private key, if a private key was generated in Connexo or imported from an external system (see Key Encryption Methods)
 * - a csr, if a key is managed nby Connexo, CSR creation is possible.
 * - TBD: a CRL, if the certificate is a trusted certificate, it will belong in a truststore and can be associated to the issuers CRL. ->
 * - Signing parameters: in case Connexo needs to trigger certificate generated, additional parameters might be required -> Up to CAImpl to determine these parameters based on KeyType
 *
 */
@ProviderType
public interface CertificateWrapper extends HasDynamicPropertiesWithUpdatableValues, HasId {
    /**
     * A certificate alias is the name given to a certificate located in the certificate store.
     * Each entry in the certificate store has an alias to help identify it.
     * @return This certificate's alias
     */
    String getAlias();

    /**
     * Set the alias for this certificate.
     * A certificate alias is the name given to a certificate located in the certificate store.
     * A Certificate alias is unique in scope of a trust store, if the certificate is a trusted certificate.
     * For non-trusted certificates, the alias is unique system-wide (among other non-trusted certificates)
     * @param alias
     */
    void setAlias(String alias);

    /**
     * If a Certificate is available in this wrapper, it will be returned, if not, Optional.empty() will be returned.
     * The certificate could be empty if this placeholder has issued a CSR but no certificate has been received/imported yet.
     * @return Certificate, if present.
     */
    Optional<X509Certificate> getCertificate();

    /**
     * The expiration time is the {@link java.time.Instant) after which this certificate expires. This value is copied from the certificate itself
     * and explicitly modelled to allow easy querying.
     * @return {@link java.time.Instant) after which this certificate is no longer valid, or Optional.empty() is this wrapper does not hold a certificate
     */
    Optional<Instant> getExpirationTime();

    /**
     * Sets a value for the certificate. Any existing value will be overridden.
     */
    void setCertificate(X509Certificate certificate);

    /**
     * Deletes this wrapper and the contained certificate and private key, if applicable.
     */
    void delete();

    /**
     * Persist changes to this object
     */
    void save();
}
