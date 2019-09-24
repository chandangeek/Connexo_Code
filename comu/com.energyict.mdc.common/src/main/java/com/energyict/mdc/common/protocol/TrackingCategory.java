/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.protocol;

import com.elster.jupiter.nls.TranslationKey;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Created by bvn on 3/14/16.
 */
public enum TrackingCategory implements TranslationKey {
    manual("trackingCategory.manual", "Manual"),
    serviceCall("trackingCategory.serviceCall", "Service call");

    private final String key;
    private final String defaultFormat;

    TrackingCategory(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

    public static Optional<TrackingCategory> fromKey(String key) {
        return Stream.of(TrackingCategory.values()).filter(cat -> cat.getKey().equals(key)).findFirst();
    }

}
