/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.autoreschedule.impl;

import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {
    SUBSCRIBER_NAME(AutoRescheduleTaskFactory.SUBSCRIBER_NAME, AutoRescheduleTaskFactory.SUBSCRIBER_DISPLAY_NAME),
    END_DEVICE_GROUP_SELECTOR("groupSelector", "Device group"),
    COMTASK_SELECTOR("comTaskSelector", "Communication tasks"),
    RETRY_COMTASKS_COMPLETED(AutoRescheduleTaskFactory.NAME + ".completedForGroup",
            "Task completed for group ''{0}'' and {1} failed communication tasks");

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
