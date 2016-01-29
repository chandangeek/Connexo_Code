package com.elster.insight.usagepoint.config.impl.errors;

import com.elster.insight.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {
    FAIL_MANAGE_CPS_ON_ACTIVE_M_CONFIG(1, Keys.FAIL_MANAGE_CPS_ON_ACTIVE_M_CONFIG, "You cannot manage custom attribute sets because metrology configuration is active."),
    OBJ_MUST_HAVE_UNIQUE_NAME(2, Keys.OBJ_MUST_HAVE_UNIQUE_NAME, "Name must be unique")
    ;

    private int number;
    private String key;
    private String defaultFormat;

    MessageSeeds(int number, String key, String defaultFormat) {
        this.number = number;
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getModule() {
        return UsagePointConfigurationService.COMPONENTNAME;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

    @Override
    public Level getLevel() {
        return Level.SEVERE;
    }

    public static class Keys {
        private Keys() {}

        public static final String FAIL_MANAGE_CPS_ON_ACTIVE_M_CONFIG = "fail.manage.cps.on.active.m.config";
        public static final String OBJ_MUST_HAVE_UNIQUE_NAME = "name.must.be.unique";
    }
}
