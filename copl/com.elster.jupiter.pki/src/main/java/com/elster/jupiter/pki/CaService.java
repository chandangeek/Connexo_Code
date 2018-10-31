/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki;

import aQute.bnd.annotation.ProviderType;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;

import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Optional;

/**
 * This interface is the holder of CA functionality. Customization can deploy its own implementation of this interface
 * to integrate with an external CA. Configuration for an external CA can be done in a separate component. Place an
 * artificial @service-constraint to make sure the config is enabled before the custom implementation is activated.
 */
@ProviderType
public interface CaService {

    String COMPONENTNAME = "PKI";

    /**
     * Indicates whether the service is configured or not.
     */
    boolean isConfigured();

    /**
     * Sends a PKCS#10 CSR to the CA, using the parameters as defined in felix config.
     *
     * @param pkcs10 PKCS10 CSR with valid signature
     * @return A signed X509 certificate
     * @throws Exception if signing was refused
     */
    X509Certificate signCsr(PKCS10CertificationRequest pkcs10, Optional<CertificateRequestData> certificateUserData);

    /**
     * revokes a certificate as defined by issuer and serial number with the provided reason.
     *
     * @param certificateTemplate the X509 certificate template that needs to be revoked. Only parts of this certificate
     * may have been filled in, those can be used to identify the certificate in the CA.
     * @param reason Values are defined in org.bouncycastle.asn1.x509.CRLReason
     */
    void revokeCertificate(CertificateAuthoritySearchFilter certificateTemplate, int reason);

    /**
     * Checks the revocation status of a certificate as defined by the template. If the template has e.g. serialnumber
     * and issuer filled in, those two parameters will be used to identify a certificate
     *
     * @param searchFilter the X509 certificate template that needs to be revoked. Only parts of this certificate
     * may have been filled in, those can be used to identify the certificate in the CA.
     * @return If a unique certificate was identified by the searchFilter, returns the revocation reason if the certificate
     * was revoked.
     */
    RevokeStatus checkRevocationStatus(CertificateAuthoritySearchFilter searchFilter);

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

    /**
     * Retrieves list of available CAs
     *
     * @return list of active CAs
     */
    List<String> getPkiCaNames();

    /**
     * Retrieves PKI instance information
     *
     * @return PKI instance related information
     */
    String getPkiInfo();

}

