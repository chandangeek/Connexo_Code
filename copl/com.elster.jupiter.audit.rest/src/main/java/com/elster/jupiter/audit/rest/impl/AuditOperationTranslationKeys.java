/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.audit.rest.impl;


import com.elster.jupiter.audit.AuditOperationType;
import com.elster.jupiter.nls.TranslationKey;

public enum AuditOperationTranslationKeys implements TranslationKey {

    INSERT(AuditOperationType.INSERT, "New version"),
    UPDATE(AuditOperationType.UPDATE, "Changed attributes"),
    DELETE(AuditOperationType.DELETE, "Remove");

    private AuditOperationType key;
    private String defaultFormat;

    AuditOperationTranslationKeys(AuditOperationType key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return this.key.type();
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }
}
