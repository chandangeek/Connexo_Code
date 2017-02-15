/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.dlms.g3.properties;


import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.HexString;
import com.energyict.mdc.dynamic.PropertySpecService;

import com.energyict.protocolimplv2.g3.common.G3Properties;

public class AS330DConfigurationSupport extends G3Properties {

    public static final String READCACHE_PROPERTY = "ReadCache";
    public static final String MIRROR_LOGICAL_DEVICE_ID = "MirrorLogicalDeviceId";
    public static final String GATEWAY_LOGICAL_DEVICE_ID = "GatewayLogicalDeviceId";
    private final HexString DEFAULT_PSK = new HexString("");

    public AS330DConfigurationSupport(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(propertySpecService, thesaurus);
    }

}