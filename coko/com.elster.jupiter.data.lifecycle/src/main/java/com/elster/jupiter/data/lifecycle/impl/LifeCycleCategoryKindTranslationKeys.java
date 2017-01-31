/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.data.lifecycle.impl;

import com.elster.jupiter.data.lifecycle.LifeCycleCategoryKind;
import com.elster.jupiter.nls.TranslationKey;

import java.util.Optional;
import java.util.stream.Stream;

enum LifeCycleCategoryKindTranslationKeys implements TranslationKey {
    INTERVAL(LifeCycleCategoryKind.INTERVAL, "Interval data"),
    DAILY(LifeCycleCategoryKind.DAILY, "Day / Month profiles"),
    REGISTER(LifeCycleCategoryKind.REGISTER, "Register data"),
    ENDDEVICEEVENT(LifeCycleCategoryKind.ENDDEVICEEVENT, "Event data"),
    LOGGING(LifeCycleCategoryKind.LOGGING, "Logging data"),
    JOURNAL(LifeCycleCategoryKind.JOURNAL, "Journal tables"),
    WEBSERVICES(LifeCycleCategoryKind.WEBSERVICES, "Web services")
    ;

    private final LifeCycleCategoryKind kind;
    private final String defaultFormat;

    LifeCycleCategoryKindTranslationKeys(LifeCycleCategoryKind kind, String defaultFormat) {
        this.kind = kind;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return Constants.DATA_LIFECYCLE_CATEGORY_NAME_PREFIX + this.kind.name();
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

    static Optional<LifeCycleCategoryKindTranslationKeys> from(LifeCycleCategoryKind kind) {
        return Stream.of(values()).filter(each -> each.kind.equals(kind)).findFirst();
    }

    public static class Constants {
        static final String DATA_LIFECYCLE_CATEGORY_NAME_PREFIX = "data.lifecycle.category.";
    }

}