/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.rest.impl;

import java.math.BigInteger;
import java.time.Instant;

public class CertificateInfo {

    public String alias;
    public Instant expirationDate;
    public String type;
    public String issuer;
    public String subject;
    public String status;
    public Integer version;
    public BigInteger serialNumber;
    public Instant notBefore;
    public Instant notAfter;
    public String signatureAlgorithm;
}