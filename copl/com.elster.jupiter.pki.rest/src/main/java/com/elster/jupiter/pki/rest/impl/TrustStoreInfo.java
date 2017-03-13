/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.rest.impl;

import com.elster.jupiter.pki.TrustStore;

public class TrustStoreInfo {

    public String name;
    public String description;

    public TrustStoreInfo() {
    }

    public TrustStoreInfo(TrustStore trustStore) {
        this.name = trustStore.getName();
        this.description = trustStore.getDescription();
    }

}
