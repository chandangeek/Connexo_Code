/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.demo.customtask.impl;

import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {
    SUBSCRIBER_NAME(DemoCustomTaskFactory.SUBSCRIBER_NAME, DemoCustomTaskFactory.SUBSCRIBER_DISPLAY_NAME),
    SEPARATOR("MDMDemoCustomTask.separator", "Separator"),
    COUNT("MDMDemoCustomTask.count", "Count"),
    SELECTOR("MDMDemoCustomTask.selector", "Selector"),
    CUSTOM_TASK_COMPLETED("MDMDemoCustomTask.completedForGroup", "Task completed for ''{0}'' group");

    private String key;
    private String defaultFormat;

    TranslationKeys(String key, String defaultFormat) {
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
