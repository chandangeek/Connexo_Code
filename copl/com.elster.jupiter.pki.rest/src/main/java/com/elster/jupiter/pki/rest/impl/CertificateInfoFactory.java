/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.rest.impl;

import com.elster.jupiter.pki.CertificateWrapper;

import java.util.List;
import java.util.stream.Collectors;

public class CertificateInfoFactory {

    public List<CertificateInfo> asInfo(List<? extends CertificateWrapper> certificates) {
        return certificates.stream()
                .map(this::asInfo)
                .sorted((c1, c2) -> c1.alias.compareToIgnoreCase(c2.alias))
                .collect(Collectors.toList());
    }

    public CertificateInfo asInfo(CertificateWrapper certificate) {
        CertificateInfo info = new CertificateInfo();
        info.alias = certificate.getAlias();
        info.expirationDate = certificate.getExpirationTime().orElse(null);
        return info;
    }
}
