/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.demo.customtask.impl;

import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {
    SUBSCRIBER_NAME(DemoCustomTaskFactory.SUBSCRIBER_NAME, DemoCustomTaskFactory.SUBSCRIBER_DISPLAY_NAME),
    SEPARATOR("MDCDemoCustomTask.separator", "Separator"),
    COUNT("MDCDemoCustomTask.count", "Count"),
    SELECTOR("MDCDemoCustomTask.selector", "Selector"),
    CUSTOM_TASK_COMPLETED("MDCDemoCustomTask.completedForGroup", "Task completed for ''{0}'' group");

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
