/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.rest.impl;

import com.elster.jupiter.pki.TrustStore;

public class TrustStoreInfo {

    public long id;
    public String name;
    public String description;
    public long version;

    public TrustStoreInfo() {
    }

    public TrustStoreInfo(TrustStore trustStore) {
        this.id = trustStore.getId();
        this.name = trustStore.getName();
        this.description = trustStore.getDescription();
        this.version = trustStore.getVersion();
    }

}
