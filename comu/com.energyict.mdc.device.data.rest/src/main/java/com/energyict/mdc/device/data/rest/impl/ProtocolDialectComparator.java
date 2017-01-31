/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;

import java.util.Comparator;


public class ProtocolDialectComparator implements Comparator<ProtocolDialectConfigurationProperties> {

    @Override
    public int compare(ProtocolDialectConfigurationProperties o1, ProtocolDialectConfigurationProperties o2) {
        return o1.getDeviceProtocolDialect().getDisplayName().compareToIgnoreCase(o2.getDeviceProtocolDialect().getDisplayName());
    }
}

