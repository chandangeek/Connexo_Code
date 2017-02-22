/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki;

/**
 * ClientCertificate differs from a regular certificate in that it can be used to proof identify, this means, a private
 * key is associated with this certificate.
 * Because of this, the complete certificate lifecycle can be managed by Connexo (generated csr, certificate, revocation, renewal)
 */
public interface ClientCertificate extends RenewableCertificate {

    /**
     * As a client certificate, this certificate is bundled with a private key.
     * Access to the private key is effectuated through this method.
     * @return The private key wrapper, containing the actual private key.
     */
    PrivateKeyWrapper getPrivateKey();

}
