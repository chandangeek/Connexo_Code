/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.rest.impl;

import java.math.BigInteger;
import java.time.Instant;

public class CertificateWrapperInfo {
    public long id;
    public boolean hasCSR;
    public boolean hasCertificate;
    public boolean hasPrivateKey;

    public String status;
    public String alias;
    public Instant expirationDate;
    public String keyEncryptionMethod;

    public CertificateInfo certificate;
    public CertificateInfo csr;

    class CertificateInfo {
        public String type;
        public String issuer;
        public String subject;
        public Integer version;
        public BigInteger serialNumber;
        public Instant notBefore;
        public Instant notAfter;
        public String signatureAlgorithm;
    }
}