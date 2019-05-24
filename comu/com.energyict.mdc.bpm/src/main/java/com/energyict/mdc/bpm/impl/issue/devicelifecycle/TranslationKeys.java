/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.bpm.impl.issue.devicelifecycle;

import com.elster.jupiter.nls.TranslationKey;

/**
 * Created by dragos on 2/18/2016.
 */
public enum TranslationKeys implements TranslationKey {
    DEVICE_LIFECYCLE_ISSUE_ASSOCIATION_PROVIDER(IssueLifecycleProcessAssociationProvider.ASSOCIATION_TYPE, "Device lifecycle issue"),
    DEVICE_LIFECYCLE_ISSUE_REASON_TITLE("issueReasons", "Issue reasons"),
    DEVICE_LIFECYCLE_ISSUE_REASON_COLUMN("issueReason", "Issue reason");

    private final String key;
    private final String defaultFormat;

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
