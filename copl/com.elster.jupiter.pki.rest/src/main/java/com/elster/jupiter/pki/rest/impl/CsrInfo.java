/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.rest.impl;

import com.elster.jupiter.pki.CertificateRequestData;

import java.util.Optional;

/**
 * Created by bvn on 3/24/17.
 */
public class CsrInfo {
    public long version;
    public String alias;
    public Long keyTypeId;
    public String keyEncryptionMethod;
    public String CN;
    public String OU;
    public String O;
    public String L;
    public String ST;
    public String C;
    public String caName;
    public String endEntityName;
    public String certificateProfileName;
    public String subjectDnFields;

    public Optional<CertificateRequestData> getCertificateRequestData() {
        if (caName != null) {
            return Optional.of(new CertificateRequestData(caName, endEntityName, certificateProfileName, subjectDnFields));
        }
        return Optional.empty();
    }

    public void setCertificateRequestData(Optional<CertificateRequestData> certificateRequestData) {
        if (certificateRequestData.isPresent()) {
            CertificateRequestData requestData = certificateRequestData.get();
            this.endEntityName = requestData.getEndEntityName();
            this.caName = requestData.getCaName();
            this.certificateProfileName = requestData.getCertificateProfileName();
        }
    }

}
