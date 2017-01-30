package com.elster.jupiter.pki;

import com.elster.jupiter.pki.impl.CertificateSearchFilter;

import aQute.bnd.annotation.ConsumerType;
import org.bouncycastle.asn1.pkcs.CertificationRequest;

import java.security.cert.CRLReason;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Optional;

/**
 * This interface is the holder of CA functionality. Customization can deploy its own implementation of this interface
 * to integrate with an external CA. Configuration for an external CA can be done in a separate component. Place an
 * artificial @service-constraint to make sure the config is enabled before the custom implementation is activated.
 */
@ConsumerType
public interface CA {
    /**
     * Send a PKCS#10 CSR to the CA, using the parameters as defined in the SigningAuthority.
     *
     * @param signingAuthorithy Defines the values for CA. These are CA specific and can not be added directly to this
     * interface but are instead modeled as custom properties
     * @param pkcs10 PKCS10 CSR with valid signature
     * @return A signed X509 certificate
     * @throws Exception if signing was refused or failed.
     */
    Optional<X509Certificate> generateCertificate(SigningParameters signingAuthorithy, CertificationRequest pkcs10);

    /**
     * revokes a certificate as defined by issuer and serial number with the provided reason.
     *
     * @param certificateTemplate the X509 certificate template that needs to be revoked. Only parts of this certificate
     * may have been filled in, those can be used to identify the certificate in the CA.
     * @param reason Values are defined in org.bouncycastle.asn1.x509.CRLReason
     */
    void revokeCertificate(CertificateSearchFilter certificateTemplate, int reason);


    /**
     * Checks the revocation status of a certificate as defined by the template. If the template has e.g. serialnumber
     * and issuer filled in, those two parameters will be used to identify a certificate
     *
     * @param searchFilter the X509 certificate template that needs to be revoked. Only parts of this certificate
     * may have been filled in, those can be used to identify the certificate in the CA.
     * @return If a unique certificate was identified by the searchFilter, returns the revocation reason if the certificate
     * was revoked, Optional.empty() otherwise.
     */
    Optional<CRLReason> checkRevocationStatus(CertificateSearchFilter searchFilter);

    /**
     * Retrieves the latest CA path. The whole certificate chain is returned.
     *
     * @param caName a unique CA name
     * @return a collection of X509Certificates with CA certificate in pos 0, and possible higer-level CA in pos 1 and
     * upwards.
     */
    List<X509Certificate> findCertificateChainByCa(String caName);

    /**
     * Retrieves the latest certificate path for a user (meter, dc, manufacterer). The whole certificate chain is returned.
     *
     * @param userName a unique user name, derived from subject DN.
     * @return a collection of X509Certificates with user certificate in pos 0, subCa at level 1 and possible higer-level
     * CA in pos 2 and upwards.
     */
    List<X509Certificate> findCertificateChainByUser(String userName);

    /**
     * Retrieves a valid certificate as defined by the fields in the CertificateTemplate, that is, defined by serial
     * number, issuer and/or subject DN.
     *
     * @return Certificate if any could be found, Optional.Empty otherwise.
     */
    Optional<X509Certificate> findCertificate(CertificateSearchFilter certificateTemplate);

    /**
     * Retrieves the latest CRL issued by the given CA.
     *
     * @param caname the name in EJBCA of the CA that issued the desired CRL
     * @return the latest CRL issued for the CA
     */
    Optional<X509CRL> getLatestCRL(String caname);

    /**
     * Retrieves the latest DELTA CRL issued by the given CA.
     *
     * @param caname the name in EJBCA of the CA that issued the desired CRL
     * @return the latest CRL issued for the CA
     */
    Optional<X509CRL> getLatestDeltaCRL(String caname);
}

