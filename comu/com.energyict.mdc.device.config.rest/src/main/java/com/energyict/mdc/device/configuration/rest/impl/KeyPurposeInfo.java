/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.pki.KeyPurpose;

public class KeyPurposeInfo {

    public String key;
    public String name;

    public KeyPurposeInfo() {
    }

    public KeyPurposeInfo(KeyPurpose keyPurpose) {
        this.key = keyPurpose.getId();
        this.name = keyPurpose.getName();
    }
}
