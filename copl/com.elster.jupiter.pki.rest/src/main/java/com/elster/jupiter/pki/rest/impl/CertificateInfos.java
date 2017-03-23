/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.rest.impl;

import com.elster.jupiter.pki.CertificateWrapper;

import java.util.ArrayList;
import java.util.List;

public class CertificateInfos {

    public int total;
    public List<CertificateInfo> certificates = new ArrayList<>();

    public CertificateInfos() {
    }

    public CertificateInfos(Iterable<? extends CertificateWrapper> certificates) {
        for (CertificateWrapper each : certificates) {
            this.certificates.add( new CertificateInfo(each) );
            total++;
        }
    }

}
