/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.lifecycle.device.microchecks;

import com.elster.jupiter.nls.TranslationKey;

public enum CustomMicroCheck implements TranslationKey {
    SKP_NOT_RUNNING("demo.lifecycle.device.microchecks.skpProcessNotRunning", "Custom Connexo Flow process isn''t running"),
    SKP_NOT_RUNNING_DESCRIPTION("demo.lifecycle.device.microchecks.skpProcessNotRunning.description", "Check if the custom BPM process ''{0}'' isn''t running.");

    private final String key;
    private final String defaultFormat;

    CustomMicroCheck(String key, String defaultFormat) {
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
