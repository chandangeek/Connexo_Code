/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.protocol.api.ConnectionType;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-09-07 (11:48)
 */
public enum TranslationKeys implements TranslationKey {

    DEFAULT("Default", "Default"),
    CHANNEL("com.energyict.mdc.device.config.ChannelSpec", "Channel"),
    REGISTER("com.energyict.mdc.device.config.RegisterSpec", "Register"),
    INBOUND(ConnectionType.Direction.INBOUND.name(), "Inbound"),
    OUTBOUND(ConnectionType.Direction.OUTBOUND.name(), "Outbound"),
    HAS_SOLVED("Solved", "Solved"),
    HAS_UNSOLVED("Unsolved","Unsolved");

    private final String key;
    private final String defaultFormat;

    TranslationKeys(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    public String translateWith(Thesaurus thesaurus){
        return thesaurus.getFormat(this).format();
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }
}