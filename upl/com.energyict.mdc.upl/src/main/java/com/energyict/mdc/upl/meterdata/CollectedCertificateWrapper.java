package com.energyict.mdc.upl.meterdata;

import java.util.Date;

/**
 * Models simple collected data for the creation of a new {@link com.energyict.mdc.upl.security.CertificateWrapper}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-30 (12:37)
 */
public interface CollectedCertificateWrapper {
    String getBase64Certificate();
    String getCertificateSserialNumber();
    String getCertificateIssuerDistinguishedName();
    Date getCertificateExpireDate();
}