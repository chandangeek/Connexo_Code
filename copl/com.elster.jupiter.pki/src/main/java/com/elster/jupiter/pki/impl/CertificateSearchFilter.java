package com.elster.jupiter.pki.impl;

import java.math.BigInteger;

/**
 * Serves mainly as struct to hold certificate identifiers, without the need to obtain the actual certificate. This
 * struct will serve as search filter.
 */
public interface CertificateSearchFilter {

    /**
     * Serial number
     */
    BigInteger getSerialNumber();

    /**
     * Name of the Issuer
     */
    String getIssuerDN();

    /**
     * Name of the Subject
     */
    String getSubjectDN();

}
