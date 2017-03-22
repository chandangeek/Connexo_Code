/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.rest.impl;

import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.pki.TrustedCertificate;

import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.stream.Collectors;

public class TrustedCertificateInfoFactory {

    public List<TrustedCertificateInfo> asInfo(TrustStore trustStore, UriInfo uriInfo) {
        return trustStore.getCertificates().stream()
                .map(certificate -> asInfo(certificate, uriInfo))
                .sorted((c1, c2) -> c1.alias.compareToIgnoreCase(c2.alias))
                .collect(Collectors.toList());
    }

    public TrustedCertificateInfo asInfo(TrustedCertificate certificate, UriInfo uriInfo) {
        TrustedCertificateInfo info = new TrustedCertificateInfo();
        info.alias = certificate.getAlias();
        info.expirationDate = certificate.getExpirationTime().orElse(null);
        return info;
    }
}
