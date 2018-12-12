/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl.adapters;

import com.elster.jupiter.nls.TranslationKey;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-27 (11:29)
 */
public enum TranslationKeys implements TranslationKey {

    DEVICE_TIME_ZONE("Adapter.property.device.timezone", "Device timezone"),
    NODE_ID("Adapter.property.nodeId", "Node id"),
    CALL_HOME_ID("Adapter.property.callHomeId", "Call home id"),
    ADDRESS("Adapter.property.address", "Address"),
    LEGACY_PROTOCOL("AdapterDeviceProtocolDialect", "Default");

    private final String uniqueName;
    private final String defaultFormat;

    TranslationKeys(String uniqueName, String defaultFormat) {
        this.uniqueName = uniqueName;
        this.defaultFormat = defaultFormat;
    }

    public String getName() {
        return uniqueName;
    }


    @Override
    public String getKey() {
        return this.getName();
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

}