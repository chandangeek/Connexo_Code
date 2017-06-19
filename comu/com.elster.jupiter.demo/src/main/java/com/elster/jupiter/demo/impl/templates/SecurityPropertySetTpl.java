/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.demo.impl.builders.SecurityPropertySetBuilder;
import com.energyict.mdc.device.config.SecurityPropertySet;

import java.math.BigDecimal;

public enum SecurityPropertySetTpl implements Template<SecurityPropertySet, SecurityPropertySetBuilder> {
    NO_SECURITY("No security",
            1,
            0,  //NO_AUTHENTICATION
            0  //NO_ENCRYPTION
    ),
    HIGH_LEVEL("High level authentication (MD5) and encryption",
            1,
            3,  //HIGH_LEVEL_MD5
            3  //DATA_AUTHENTICATION_ENCRYPTION
    ),
    HIGH_LEVEL_NO_ENCRYPTION_MD5("High level authentication (MD5) - No encryption",
            1,
            3,  //HIGH_LEVEL_MD5
            0  //NO_ENCRYPTION
    ),
    HIGH_LEVEL_NO_ENCRYPTION_GMAC("High level authentication - No encryption",
            1,
            5,  //HIGH_LEVEL_GMAC
            0  //NO_ENCRYPTION
    )
    ;
    private String name;
    private int client;
    private int authLevel;
    private int encLevel;

    SecurityPropertySetTpl(String name, int client, int authLevel, int encLevel) {
        this.name = name;
        this.client = client;
        this.authLevel = authLevel;
        this.encLevel = encLevel;
    }

    @Override
    public Class<SecurityPropertySetBuilder> getBuilderClass() {
        return SecurityPropertySetBuilder.class;
    }

    @Override
    public SecurityPropertySetBuilder get(SecurityPropertySetBuilder builder) {
        return builder.withClient(BigDecimal.valueOf(client)).withAuthLevel(authLevel).withEncLevel(encLevel).withName(name);
    }

    public String getName() {
        return name;
    }
}
