/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.demo.impl.builders.SecurityPropertySetBuilder;
import com.energyict.mdc.device.config.DeviceSecurityUserAction;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.protocol.api.device.messages.DlmsAuthenticationLevelMessageValues;
import com.energyict.mdc.protocol.api.device.messages.DlmsEncryptionLevelMessageValues;

import java.util.Arrays;
import java.util.List;

public enum SecurityPropertySetTpl implements Template<SecurityPropertySet, SecurityPropertySetBuilder> {
    NO_SECURITY("No security",
            DlmsAuthenticationLevelMessageValues.NO_AUTHENTICATION.getValue(),
            DlmsEncryptionLevelMessageValues.NO_ENCRYPTION.getValue(),
            "1"
    ),
    HIGH_LEVEL("High level authentication (MD5) and encryption",
            DlmsAuthenticationLevelMessageValues.HIGH_LEVEL_MD5.getValue(),
            DlmsEncryptionLevelMessageValues.DATA_AUTHENTICATION_ENCRYPTION.getValue(),
            "1"
    ),
    HIGH_LEVEL_NO_ENCRYPTION_MD5("High level authentication (MD5) - No encryption",
            DlmsAuthenticationLevelMessageValues.HIGH_LEVEL_MD5.getValue(),
            DlmsEncryptionLevelMessageValues.NO_ENCRYPTION.getValue(),
            "1"

    ),
    HIGH_LEVEL_NO_ENCRYPTION_GMAC("High level authentication - No encryption",
            DlmsAuthenticationLevelMessageValues.HIGH_LEVEL_GMAC.getValue(),
            DlmsEncryptionLevelMessageValues.NO_ENCRYPTION.getValue(),
            "1"
    );

    private String name;
    private int authLevel;
    private int encLevel;
    private String client;

    SecurityPropertySetTpl(String name, int authLevel, int encLevel, String client) {
        this.name = name;
        this.authLevel = authLevel;
        this.encLevel = encLevel;
        this.client = client;
    }

    @Override
    public Class<SecurityPropertySetBuilder> getBuilderClass() {
        return SecurityPropertySetBuilder.class;
    }

    @Override
    public SecurityPropertySetBuilder get(SecurityPropertySetBuilder builder) {
        return builder
                .withAuthLevel(authLevel)
                .withEncLevel(encLevel)
                .withClient(client)
                .withName(name);
    }

    public String getName() {
        return name;
    }
}
