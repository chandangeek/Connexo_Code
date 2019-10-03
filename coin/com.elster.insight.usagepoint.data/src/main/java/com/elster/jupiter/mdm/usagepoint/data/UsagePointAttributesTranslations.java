package com.elster.jupiter.mdm.usagepoint.data;

import com.elster.jupiter.nls.TranslationKey;

public enum UsagePointAttributesTranslations implements TranslationKey {

        USAGE_POINT_NAME("usagePointName", "Usage point name"),
        USAGE_POINT_MRID("usagePointMrID", "Usage point mrID");


        private final String key;
        private final String defaultFormat;

        UsagePointAttributesTranslations (String key, String defaultFormat) {
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
