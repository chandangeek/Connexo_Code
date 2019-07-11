/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservice.issue.impl.actions.process;

import com.elster.jupiter.nls.TranslationKey;

/**
 * Created by dragos on 2/18/2016.
 */
public enum TranslationKeys implements TranslationKey {
    WEBSERVICE_ISSUE_ASSOCIATION_PROVIDER(WebServiceIssueProcessAssociationProvider.ASSOCIATION_TYPE, "Web service issue"),
    WEBSERVICE_ISSUE_REASON_TITLE(WebServiceIssueProcessAssociationProvider.PROPERTY_REASONS, "Issue reasons"),
    WEBSERVICE_ISSUE_REASON_COLUMN("issueReason", "Issue reason");

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
