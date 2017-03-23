/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.rest.impl;

import com.elster.jupiter.pki.CertificateWrapper;

import java.time.Instant;

public class CertificateInfo {

    public String alias;
    public Instant expirationDate;
    public String type;
    public String issuer;
    public String subject;
    public String status;

    CertificateInfo() {
    }

    CertificateInfo(CertificateWrapper certificateWrapper) {
        this.alias = certificateWrapper.getAlias();
        this.expirationDate = certificateWrapper.getExpirationTime().orElse(null);
    }
}