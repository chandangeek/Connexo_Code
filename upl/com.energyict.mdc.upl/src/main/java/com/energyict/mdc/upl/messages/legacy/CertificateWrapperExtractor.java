/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.upl.messages.legacy;

import com.energyict.mdc.upl.security.CertificateWrapper;

import java.security.cert.X509Certificate;

public interface CertificateWrapperExtractor {

    X509Certificate getCertificate(CertificateWrapper certificateWrapper);

    String getBase64EncodedCertificate(CertificateWrapper certificateWrapper);

}