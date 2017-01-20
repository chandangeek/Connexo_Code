package com.elster.jupiter.pki;

import aQute.bnd.annotation.ProviderType;

/**
 * This object represents a Certificate stored in Connexo. A Certificate can optionally linked to
 * - a private key, if a private key was generated in Connexo or imported from an external system (see Key Encryption Methods)
 * - a csr, if a key is managed nby Connexo, CSR creation is possible.
 * - TBD: a CRL, if the certificate is a trusted certificate, it will belong in a truststore and can be associated to the issuers CRL.
 * - Signing parameters: in case Connexo needs to trigger certificate generated, additional parameters might be required.
 */
@ProviderType
public interface CertificateWrapper {
}
