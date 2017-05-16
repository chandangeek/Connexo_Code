/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.pki.KeyType;

public class KeyTypeInfo {
    public long id;
    public String name;
    public boolean isKey;
    public boolean requiresDuration;
    public boolean requiresKeyEncryptionMethod;

    public KeyTypeInfo() {
    }

    public KeyTypeInfo(KeyType keyType) { // TODO introduce factory
        this.id = keyType.getId();
        this.name = keyType.getName();
        this.requiresDuration = keyType.getCryptographicType().requiresDuration();
        this.isKey = keyType.getCryptographicType().isKey();
        this.requiresKeyEncryptionMethod = keyType.getCryptographicType().requiresKeyEncryptionMethod();
    }
}
