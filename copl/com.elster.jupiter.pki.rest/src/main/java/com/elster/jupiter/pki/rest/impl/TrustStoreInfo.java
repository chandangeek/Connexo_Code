/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.rest.impl;

public class TrustStoreInfo {

    public long id;
    public String name;
    public String description;
    public long version;
    public Integer keyStoreFileSize; // used to validate keyStoreFile for importing trusted certificates

    public TrustStoreInfo() {
    }
}
