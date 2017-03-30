/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.bpm.impl.issue.datacollection;

import com.elster.jupiter.nls.TranslationKey;

/**
 * Created by dragos on 2/18/2016.
 */
public enum TranslationKeys implements TranslationKey {
    DATA_COLLECTION_ISSUE_ASSOCIATION_PROVIDER(IssueProcessAssociationProvider.ASSOCIATION_TYPE, "Data collection issue"),
    DATA_COLLECTION_ISSUE_REASON_TITLE("issueReasons", "Issue reasons"),
    DATA_COLLECTION_ISSUE_REASON_COLUMN("issueReason", "Issue reason");

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
