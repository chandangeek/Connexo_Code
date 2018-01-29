/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.impl;

import java.math.BigInteger;

public class CertificateSearchFilterImpl implements CertificateSearchFilter {
    private BigInteger serialNumber;
    private String issuerDN;
    private String subjectDN;

    public void setSerialNumber(BigInteger serialNumber) {
        this.serialNumber = serialNumber;
    }

    public void setIssuerDN(String issuerDN) {
        this.issuerDN = issuerDN;
    }

    public void setSubjectDN(String subjectDN) {
        this.subjectDN = subjectDN;
    }

    @Override
    public BigInteger getSerialNumber() {
        return serialNumber;
    }

    @Override
    public String getIssuerDN() {
        return issuerDN;
    }

    @Override
    public String getSubjectDN() {
        return subjectDN;
    }
}
