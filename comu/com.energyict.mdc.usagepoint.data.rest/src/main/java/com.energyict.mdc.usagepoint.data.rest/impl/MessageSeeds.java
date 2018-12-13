/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.usagepoint.data.rest.impl;

import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    NO_USAGE_POINT_WITH_NAME(1, "NoUsagePointWithSuchName", "No usage point with name {0}."),
    NO_METROLOGY_CONFIG_FOR_USAGE_POINT(2, "NoMetrologyConfigForUsagePoint", "Usage point {0} doesn''t have a link to metrology configuration."),
    NO_SUCH_CHANNEL_FOR_USAGE_POINT(3, "NoSuchChannelForUsagePoint", "Usage point {0} doesn''t have channel with id {1}."),
    NO_SUCH_REGISTER_FOR_USAGE_POINT(4, "NoSuchRegisterForUsagePoint","Usage point {0} doesn''t have register with id {1}.");

    private final int number;
    private final String key;
    private final String format;

    MessageSeeds(int number, String key, String format) {
        this.number = number;
        this.key = key;
        this.format = format;
    }

    @Override
    public String getModule() {
        return UsagePointApplication.COMPONENT_NAME;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return format;
    }

    @Override
    public Level getLevel() {
        return Level.SEVERE;
    }
}
