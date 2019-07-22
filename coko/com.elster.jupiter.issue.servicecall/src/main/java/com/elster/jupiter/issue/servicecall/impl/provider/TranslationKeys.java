/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.servicecall.impl.provider;

import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {
    SERVICE_CALL_ISSUE_ASSOCIATION_PROVIDER(ServiceCallIssueProcessAssociationProvider.ASSOCIATION_TYPE, "Service call issue"),
    SERVICE_CALL_ISSUE_REASON_TITLE("issueReasons", "Issue reasons"),
    SERVICE_CALL_ISSUE_REASON_COLUMN("issueReason", "Issue reason");

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
