package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;

import java.util.Comparator;


public class ProtocolDialectComparator implements Comparator<ProtocolDialectConfigurationProperties> {

    @Override
    public int compare(ProtocolDialectConfigurationProperties o1, ProtocolDialectConfigurationProperties o2) {
        return o1.getDeviceProtocolDialect().getDeviceProtocolDialectDisplayName().compareToIgnoreCase(o2.getDeviceProtocolDialect().getDeviceProtocolDialectDisplayName());
    }
}

