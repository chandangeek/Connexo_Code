/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.rest.impl;

import com.elster.jupiter.pki.CertificateRequestData;
import com.elster.jupiter.rest.util.IdWithNameInfo;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Optional;

public class CertificateWrapperInfo {
    public long id;
    public long version;
    public boolean hasCSR;
    public boolean hasCertificate;
    public boolean hasPrivateKey;

    public IdWithNameInfo status;
    public String alias;
    public Instant expirationDate;
    public String keyEncryptionMethod;

    public String type;
    public String issuer;
    public String subject;
    public Integer certificateVersion;
    @XmlJavaTypeAdapter(SerialNumberAdapter.class)
    public BigInteger serialNumber;
    public Instant notBefore;
    public Instant notAfter;
    public String signatureAlgorithm;


    public String endEntityName;
    public String caName;
    public String certProfileName;
    public String subjectDnFields;

    public void setCertificateRequestData(Optional<CertificateRequestData> certificateRequestData) {
        if (certificateRequestData.isPresent()) {
            CertificateRequestData requestData = certificateRequestData.get();
            this.endEntityName = requestData.getEndEntityName();
            this.caName = requestData.getCaName();
            this.certProfileName = requestData.getCertificateProfileName();
            this.subjectDnFields = requestData.getSubjectDNfields();
        }
    }
}
