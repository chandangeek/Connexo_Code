/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.impl.channels.ip;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;

import java.util.logging.Level;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-05 (09:11)
 */
public enum IpMessageSeeds implements MessageSeed {

    MUST_BE_POSITIVE(0, Keys.MUST_BE_POSITIVE, "Value must be positive (i.e. >= 0");

    private final int number;
    private final String key;
    private final String defaultFormat;

    IpMessageSeeds(int number, String key, String defaultFormat) {
        this.number = number;
        this.key = key;
        this.defaultFormat = defaultFormat;
    }


    @Override
    public String getModule() {
        return DeviceProtocolService.COMPONENT_NAME;
    }

    @Override
    public int getNumber() {
        return this.number;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

    @Override
    public Level getLevel() {
        return Level.SEVERE;
    }

    public static class Keys {
        public static final String MUST_BE_POSITIVE = "value.must.be.positive";
    }

}