/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.demo.impl.builders.SecurityPropertySetBuilder;
import com.energyict.mdc.device.config.SecurityPropertySet;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public enum SecurityPropertySetTpl implements Template<SecurityPropertySet, SecurityPropertySetBuilder> {
    NO_SECURITY_DEFAULT("No security",
            1,
            -1,
            0,  //NO_AUTHENTICATION
            0,  //NO_ENCRYPTION
            Arrays.asList(KeyAccessorTpl.ENCRYPTION_KEY, KeyAccessorTpl.AUTHENTICATION_KEY, KeyAccessorTpl.PASSWORD)
    ),
    NO_SECURITY_ZERO_SUITE_DEFAULT("No security",
            1,
            0, //SECURITY_SUITE_0
            0, //NO_AUTHENTICATION
            0,  //NO_ENCRYPTION
            Arrays.asList(KeyAccessorTpl.ENCRYPTION_KEY, KeyAccessorTpl.AUTHENTICATION_KEY, KeyAccessorTpl.PASSWORD)
    ),
    HIGH_LEVEL("High level authentication (MD5) and encryption",
            1,
            -1,
            3,  //HIGH_LEVEL_MD5
            3,  //DATA_AUTHENTICATION_ENCRYPTION
            Arrays.asList(KeyAccessorTpl.ENCRYPTION_KEY, KeyAccessorTpl.AUTHENTICATION_KEY, KeyAccessorTpl.PASSWORD)
    ),
    HIGH_LEVEL_NO_ENCRYPTION_MD5("High level authentication (MD5) - No encryption",
            1,
            -1,
            3,  //HIGH_LEVEL_MD5
            0,  //NO_ENCRYPTION
            Arrays.asList(KeyAccessorTpl.ENCRYPTION_KEY, KeyAccessorTpl.AUTHENTICATION_KEY, KeyAccessorTpl.PASSWORD)
    ),
    HIGH_LEVEL_NO_ENCRYPTION_GMAC("High level authentication - No encryption",
            1,
            -1,
            5,  //HIGH_LEVEL_GMAC
            0,   //NO_ENCRYPTION
            Arrays.asList(KeyAccessorTpl.ENCRYPTION_KEY, KeyAccessorTpl.AUTHENTICATION_KEY, KeyAccessorTpl.PASSWORD)
    ),
    BEACON1_GMAC("Beacon1 GMAC 5:3",
            1,
            0,  //SECURITY_SUITE_0
            5,  //HIGH_LEVEL_GMAC
            3,  //MESSAGE_ENCRYPTION_AND_AUTHENTICATION
            Arrays.asList(KeyAccessorTpl.ENCRYPTION_KEY_BEACON1, KeyAccessorTpl.AUTHENTICATION_KEY_BEACON1)
    ),
    BEACON2_GMAC("Beacon2 GMAC 5:3",
            1,
            0,  //SECURITY_SUITE_0
            5,  //HIGH_LEVEL_GMAC
            3,  //MESSAGE_ENCRYPTION_AND_AUTHENTICATION
            Arrays.asList(KeyAccessorTpl.ENCRYPTION_KEY_BEACON2, KeyAccessorTpl.AUTHENTICATION_KEY_BEACON2)
    ),
    GMAC("GMAC 5:3",
            1,
            -1,
            5,  //HIGH_LEVEL_GMAC
            3,  //MESSAGE_ENCRYPTION_AND_AUTHENTICATION
            Arrays.asList(KeyAccessorTpl.ENCRYPTION_KEY_10_YEARS, KeyAccessorTpl.AUTHENTICATION_KEY_10_YEARS, KeyAccessorTpl.PASSWORD_10_YEARS)
    ),
    PASSWORD_ONLY("Password only",
            1,
            -1,
            1,  //LOW_LEVEL
            0,  //NO_ENCRYPTION
            Arrays.asList(KeyAccessorTpl.ENCRYPTION_KEY_10_YEARS, KeyAccessorTpl.AUTHENTICATION_KEY_10_YEARS, KeyAccessorTpl.PASSWORD_10_YEARS)
    ),
    ;
    private String name;
    private int client;
    private int suite;
    private int authLevel;
    private int encLevel;
    private List<KeyAccessorTpl> keys;

    SecurityPropertySetTpl(String name, int client, int suite, int authLevel, int encLevel, List<KeyAccessorTpl> keys) {
        this.name = name;
        this.suite = suite;
        this.client = client;
        this.authLevel = authLevel;
        this.encLevel = encLevel;
        this.keys = keys;
    }

    @Override
    public Class<SecurityPropertySetBuilder> getBuilderClass() {
        return SecurityPropertySetBuilder.class;
    }

    @Override
    public SecurityPropertySetBuilder get(SecurityPropertySetBuilder builder) {
        return builder.withClient(BigDecimal.valueOf(client)).withSuite(suite).withAuthLevel(authLevel).withEncLevel(encLevel).withName(name).withKeys(keys);
    }

    public String getName() {
        return name;
    }
}
