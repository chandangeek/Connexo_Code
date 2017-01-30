package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.mdc.upl.meterdata.CollectedCertificateWrapper;

import javax.xml.bind.annotation.XmlRootElement;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Date;

/**
 * Provides an implementation for the {@link CollectedCertificateWrapper} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-30 (12:55)
 */
@XmlRootElement
public class CollectedCertificateWrapperImpl implements CollectedCertificateWrapper {

    private String base64Certificate;
    private String certificateSerialNumber;
    private String certificateIssuerDistinguishedName;
    private Date certificateExpireDate;

    public CollectedCertificateWrapperImpl(X509Certificate certificate) {
        super();
        this.base64Certificate = this.encode(certificate);
        this.certificateExpireDate = certificate.getNotAfter();
        this.certificateSerialNumber = certificate.getSerialNumber().toString();
        this.certificateIssuerDistinguishedName = certificate.getIssuerDN().getName();
    }

    private String encode(X509Certificate certificate) {
        try {
            byte[] encodedCertificate = certificate.getEncoded();
            return new String(Base64.getEncoder().encode(encodedCertificate), StandardCharsets.UTF_8);
        } catch (CertificateEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }
    @Override
    public Date getCertificateExpireDate() {
        return this.certificateExpireDate;
    }

    @Override
    public String getCertificateIssuerDistinguishedName() {
        return this.certificateIssuerDistinguishedName;
    }

    @Override
    public String getCertificateSserialNumber() {
        return this.certificateSerialNumber;
    }

    @Override
    public String getBase64Certificate() {
        return this.base64Certificate;
    }

}