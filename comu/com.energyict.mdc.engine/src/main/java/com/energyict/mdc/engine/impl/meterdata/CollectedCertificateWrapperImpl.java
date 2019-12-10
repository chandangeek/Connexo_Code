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
    private String alias;
    private String trustStoreName;

    public CollectedCertificateWrapperImpl(X509Certificate certificate) {
        super();
        this.base64Certificate = this.encode(certificate);
        this.certificateExpireDate = certificate.getNotAfter();
        this.certificateSerialNumber = certificate.getSerialNumber().toString();
        this.certificateIssuerDistinguishedName = certificate.getIssuerDN().getName();
        this.alias = certificate.getSerialNumber().toString();
    }

    public CollectedCertificateWrapperImpl(X509Certificate certificate, String alias, String trustStoreName) {
        super();
        this.base64Certificate = this.encode(certificate);
        this.certificateExpireDate = certificate.getNotAfter();
        this.certificateSerialNumber = certificate.getSerialNumber().toString();
        this.certificateIssuerDistinguishedName = certificate.getIssuerDN().getName();
        this.alias = alias;
        this.trustStoreName = trustStoreName;
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

    @Override
    public String getAlias() {
        return alias;
    }

    @Override
    public String getTrustStoreName() {
        return trustStoreName;
    }

    @Override
    public String toString() {
        StringBuilder certificateDescription = new StringBuilder("Certificate: ");
        certificateDescription.append("Alias: " + alias);
        certificateDescription.append("Base64 certificate: " + base64Certificate);
        certificateDescription.append(", Expire date: " + certificateExpireDate);
        certificateDescription.append(", IssuerDN: " + certificateIssuerDistinguishedName);
        certificateDescription.append(", Serial number: " + certificateSerialNumber);
        return certificateDescription.toString();
    }
}