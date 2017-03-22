/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.rest.impl;

import com.elster.jupiter.pki.TrustStore;

import java.util.List;

public class TrustStoreInfo {

    public long id;
    public String name;
    public String description;
    public long version;
    public List<TrustedCertificateInfo> trustedCertificates;

    public TrustStoreInfo() {
    }
}
