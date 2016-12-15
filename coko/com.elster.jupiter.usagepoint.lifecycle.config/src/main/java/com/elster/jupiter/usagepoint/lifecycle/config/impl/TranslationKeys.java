package com.elster.jupiter.usagepoint.lifecycle.config.impl;

import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointStage;

public enum TranslationKeys implements TranslationKey {

    TRANSITION_INSTALL_ACTIVE("usage.point.transition.install.active", "Install active"),
    TRANSITION_INSTALL_INACTIVE("usage.point.transition.install.inactive", "Install inactive"),
    TRANSITION_DEACTIVATE("usage.point.transition.deactivate", "Deactivate"),
    TRANSITION_ACTIVATE("usage.point.transition.activate", "Activate"),
    TRANSITION_DEMOLISH_FROM_ACTIVE("usage.point.transition.demolish.from.active", "Demolish"),
    TRANSITION_DEMOLISH_FROM_INACTIVE("usage.point.transition.demolish.from.inactive", "Demolish"),
    USAGE_POINT_STAGE_PRE_OPERATIONAL(Keys.STAGE_PREFIX + UsagePointStage.Key.PRE_OPERATIONAL, "Preoperational"),
    USAGE_POINT_STAGE_OPERATIONAL(Keys.STAGE_PREFIX + UsagePointStage.Key.OPERATIONAL, "Operational"),
    USAGE_POINT_STAGE_POST_OPERATIONAL(Keys.STAGE_PREFIX + UsagePointStage.Key.POST_OPERATIONAL, "Post operational")
    ;

    private final String key;
    private final String defaultFormat;

    TranslationKeys(String key, String defaultFormat) {
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

    static class Keys {
        private Keys() {
        }

        public static final String STAGE_PREFIX = "usage.point.stage.";
    }
}
