/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki;

import org.bouncycastle.asn1.x500.X500Name;

/**
 * ClientCertificate differs from a regular certificate in that it can be used to proof identify, this means, a private
 * key is associated with this certificate.
 * Because of this, the complete certificate lifecycle can be managed by Connexo (generated csr, certificate, revocation, renewal)
 */
public interface ClientCertificateWrapper extends RequestableCertificateWrapper {

    /**
     * As a client certificate, this certificate is bundled with a private key.
     * Access to the private key is effectuated through this method.
     * @return The private key wrapper, containing the actual private key.
     */
    PrivateKeyWrapper getPrivateKeyWrapper();

    /**
     * Obtain the key type of this certificate wrapper. The key type describes the signing algorithm for this certificate.
     * @return KeyType
     */
    KeyType getKeyType();

    /**
     * Generates a CSR with provided distinguished name. The CSR is stored in the CertificateWrapper. Any existing CSR
     * is overwritten.
     * @param subjectDN The X500Name to be used as subject DN.
     */
    void generateCSR(X500Name subjectDN);
}
