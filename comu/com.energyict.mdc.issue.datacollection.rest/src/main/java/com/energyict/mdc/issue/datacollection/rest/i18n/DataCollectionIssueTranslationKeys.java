package com.energyict.mdc.issue.datacollection.rest.i18n;

import com.elster.jupiter.nls.TranslationKey;

public enum DataCollectionIssueTranslationKeys  implements TranslationKey {

    RETRY_NOT_SUPPORTED("RetryNotSupported", "This action is not supported by these issues"),
    ISSUE_DOES_NOT_EXIST("IssueDoesNotExist", "Issue doesn't exist"),
    ISSUE_ALREADY_CLOSED("IssueAlreadyClosed", "Issue already closed")
    ;

    private final String value;
    private final String defaultFormat;

    DataCollectionIssueTranslationKeys(String value, String defaultFormat) {
        this.value = value;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

    @Override
    public String getKey() {
        return this.value;
    }
}
