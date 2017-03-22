/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.rest.impl;

import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.pki.TrustedCertificate;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TrustStoreInfoFactory {

    public List<TrustStoreInfo> asInfoList(List<TrustStore> trustStores) {
        return trustStores != null
            ? trustStores.stream().filter(Objects::nonNull).map(t -> asInfo(t)).collect(Collectors.toList())
            : Collections.emptyList();
    }

    public TrustStoreInfo asInfo(TrustStore trustStore) {
        TrustStoreInfo info = new TrustStoreInfo();
        info.id = trustStore.getId();
        info.name = trustStore.getName();
        info.description = trustStore.getDescription();
        info.version = trustStore.getVersion();
        return info;
    }
}
