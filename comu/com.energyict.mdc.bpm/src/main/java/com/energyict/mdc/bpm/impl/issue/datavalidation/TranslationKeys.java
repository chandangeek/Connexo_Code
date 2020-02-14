/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.bpm.impl.issue.datavalidation;

import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {
    DATA_VALIDATION_ISSUE_ASSOCIATION_PROVIDER(IssueDataValidationAssociationProvider.ASSOCIATION_TYPE, "Data validation issue"),
    DATA_VALIDATION_ISSUE_REASON_TITLE("issueReasons", "Issue reasons"),
    DATA_VALIDATION_ISSUE_REASON_COLUMN("issueReason", "Issue reason");

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
