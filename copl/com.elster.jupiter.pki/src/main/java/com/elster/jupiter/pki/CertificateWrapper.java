package com.elster.jupiter.pki;

import com.elster.jupiter.util.HasId;

import aQute.bnd.annotation.ProviderType;

import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;

/**
 * This object represents a Certificate stored in Connexo. A Certificate can optionally linked to
 * - a private key, if a private key was generated in Connexo or imported from an external system (see Key Encryption Methods)
 * - a csr, if a key is managed nby Connexo, CSR creation is possible.
 * - TBD: a CRL, if the certificate is a trusted certificate, it will belong in a truststore and can be associated to the issuers CRL. ->
 * - Signing parameters: in case Connexo needs to trigger certificate generated, additional parameters might be required -> Up to CAImpl to determine these parameters based on KeyType
 */
@ProviderType
public interface CertificateWrapper extends HasDynamicPropertiesWithUpdatableValues, HasId, SecurityValueWrapper {
    /**
     * A certificate alias is the name given to a certificate located in the certificate store.
     * Each entry in the certificate store has an alias to help identify it.
     *
     * @return This certificate's alias
     */
    String getAlias();

    /**
     * Set the alias for this certificate.
     * A certificate alias is the name given to a certificate located in the certificate store.
     * A Certificate alias is unique in scope of a trust store, if the certificate is a trusted certificate.
     * For non-trusted certificates, the alias is unique system-wide (among other non-trusted certificates)
     *
     * @param alias
     */
    void setAlias(String alias);

    /**
     * If a Certificate is available in this wrapper, it will be returned, if not, Optional.empty() will be returned.
     * The certificate could be empty if this placeholder has issued a CSR but no certificate has been received/imported yet.
     *
     * @return Certificate, if present.
     */
    Optional<X509Certificate> getCertificate();

    /**
     * The expiration time is the {@link java.time.Instant) after which this certificate expires. This value is copied from the certificate itself
     * and explicitly modelled to allow easy querying.
     *
     * @return {@link java.time.Instant) after which this certificate is no longer valid, or Optional.empty() is this wrapper does not hold a certificate
     */
    Optional<Instant> getExpirationTime();

    /**
     * If the wrapper contains a Certificate, this method will return a comma-separated list of key usages, containing both
     * basic and extended key usages. If the wrapper does not contain a Certificate, it will return the key usages and
     * extended key usages as found on the CSR, if present.
     *
     * @return comma-separated list of key usages, empty() is no certificate is contained.
     */
    Optional<String> getAllKeyUsages();

    /**
     * Returns a set of ExtendedKeyUsage defined on this CertificateWrapper, if present. If not present, the CSR's key extended usages
     * will be returned, if present. Empty() otherwise.
     *
     * @throws CertificateParsingException
     */
    Set<ExtendedKeyUsage> getExtendedKeyUsages() throws CertificateParsingException;

    /**
     * Returns a set of KeyUsage defined on this CertificateWrapper, if present. If not present, the CSR's key usages
     * will be returned, if present. Empty() otherwise.
     *
     * @throws CertificateParsingException
     */
    Set<KeyUsage> getKeyUsages();

    /**
     * The current version of this business object. Version property is used for concurrency purposes.
     */
    long getVersion();

    /**
     * Status describes the content of the wrapper.
     *
     * @return Translated CertificateWrapper status.
     */
    String getStatus();

    /**
     * Returns the certificate status.
     *
     * @return {@link CertificateStatus}
     */
    Optional<CertificateStatus> getCertificateStatus();

    /**
     * Sets a value for the certificate. Any existing value will be overridden.
     */
    void setCertificate(X509Certificate certificate, Optional<CertificateRequestData> certificateRequestData);

    /**
     * Deletes this wrapper and the contained certificate and private key, if applicable.
     */
    void delete();

    /**
     * Persist changes to this object
     */
    void save();

    /**
     * returns true is this wrapper contains a CSR, false otherwise
     */
    boolean hasCSR();

    /**
     * returns true is this wrapper contains a private key, false otherwise
     */
    boolean hasPrivateKey();

    /**
     * LastReadDate is the date when the certificate was last obtained from the device (using a device command)
     *
     * @return lastReadDate is the certificate was ever read, empty() otherwise
     */
    Optional<Instant> getLastReadDate();

    String getSubject();

    /**
     * Extracts Common-Name (CN) value from the certificate subject
     * @return CN
     */
    Optional<String> getSubjectCN();

    void setSubject(String subject);

    String getIssuer();

    void setIssuer(String issuer);

    void setKeyUsagesCsv(String keyUsages);

    String stringifyKeyUsages(Set<KeyUsage> keyUsages, Set<ExtendedKeyUsage> extendedKeyUsages);

    Optional<String> getStringifiedKeyUsages();

    /**
     * Set {@link CertificateWrapperStatus}, that will be used over X509Certificate statuses
     * @param status to be set
     */
    void setWrapperStatus(CertificateWrapperStatus status);

    CertificateWrapperStatus getWrapperStatus();

    Optional<CertificateRequestData> getCertificateRequestData();

    void setCertificateRequestData(Optional<CertificateRequestData> certificateRequestData);

    /**
     *
     * @return parent certificate wrapper. if issuer is different than subject it means we should find a matching parent certificate
     * that has the subject value equals with current object issuer.
     * null is returned if this is a root CA
     */
    CertificateWrapper getParent();

    default boolean isCRLSigner() {
        Optional<String> allKeyUsages = getAllKeyUsages();
        if (allKeyUsages.isPresent()) {
            String usages = allKeyUsages.get();
            String[] allUsages = usages.split(",");
            for (String usage: allUsages) {
                if ("cRLSign".equals(usage.trim())) {
                    return true;
                }
            }
        }
        return false;
    }
}
