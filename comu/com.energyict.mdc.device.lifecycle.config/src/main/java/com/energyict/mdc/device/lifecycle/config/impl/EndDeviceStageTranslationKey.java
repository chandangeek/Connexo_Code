/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.config.impl;

import com.elster.jupiter.metering.EndDeviceStage;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.StringChecker;
import com.energyict.mdc.device.lifecycle.config.DefaultCustomStateTransitionEventType;
import com.energyict.mdc.device.lifecycle.config.DefaultState;

/**
 * Represents the translation keys for the stages of states.
 */
public enum EndDeviceStageTranslationKey implements TranslationKey {

    OPERATIONAL(EndDeviceStageTranslationKey.prefix + EndDeviceStage.OPERATIONAL.name(), "Operational"),
    PRE_OPERATIONAL(EndDeviceStageTranslationKey.prefix + EndDeviceStage.PRE_OPERATIONAL.name(), "Pre-operational"),
    POST_OPERATIONAL(EndDeviceStageTranslationKey.prefix + EndDeviceStage.POST_OPERATIONAL.name(), "Post-operational");

    public final static String prefix = "dlc.stage.";
    private final String key;
    private final String defaultFormat;

    EndDeviceStageTranslationKey(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

}