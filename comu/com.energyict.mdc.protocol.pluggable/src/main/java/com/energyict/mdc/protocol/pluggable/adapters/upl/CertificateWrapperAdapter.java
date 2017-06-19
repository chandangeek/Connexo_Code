package com.energyict.mdc.protocol.pluggable.adapters.upl;

import com.energyict.mdc.upl.security.CertificateWrapper;

import java.security.KeyStore;
import java.util.Optional;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 29/05/2017 - 9:40
 */
public class CertificateWrapperAdapter implements CertificateWrapper {

    private final com.elster.jupiter.pki.CertificateWrapper certificateWrapper;
    private final Optional<KeyStore> trustStore;

    public CertificateWrapperAdapter(com.elster.jupiter.pki.CertificateWrapper certificateWrapper, Optional<KeyStore> trustStore) {
        this.certificateWrapper = certificateWrapper;
        this.trustStore = trustStore;
    }

    public com.elster.jupiter.pki.CertificateWrapper getCertificateWrapper() {
        return certificateWrapper;
    }

    public Optional<KeyStore> getTrustStore() {
        return trustStore;
    }
}