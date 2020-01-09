/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.metering.impl;

import com.elster.jupiter.nls.TranslationKey;

public enum UsagePointAttributeTranslations implements TranslationKey {

        USAGE_POINT_NAME("usagePointName", "Usage point name"),
        USAGE_POINT_MRID("usagePointMrID", "Usage point MRID");


        private final String key;
        private final String defaultFormat;

        UsagePointAttributeTranslations(String key, String defaultFormat) {
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
