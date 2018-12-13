/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.impl.checks;

import com.elster.jupiter.nls.TranslationKey;

public enum MicroCheckTranslationKeys implements TranslationKey {
    METROLOGY_CONF_IS_DEFINED_NAME(Keys.NAME_PREFIX + MetrologyConfigurationIsDefinedCheck.class.getSimpleName(), "Metrology configuration is defined"),
    METROLOGY_CONF_IS_DEFINED_MESSAGE(Keys.MESSAGE_PREFIX + MetrologyConfigurationIsDefinedCheck.class.getSimpleName(), "This check verifies that a metrology configuration is linked to a usage point."),
    METER_ROLES_ARE_SPECIFIED_NAME(Keys.NAME_PREFIX + MeterRolesAreSpecifiedCheck.class.getSimpleName(), "Meters are specified for all the roles"),
    METER_ROLES_ARE_SPECIFIED_MESSAGE(Keys.MESSAGE_PREFIX + MeterRolesAreSpecifiedCheck.class.getSimpleName(), "This check verifies that meters are specified for all of the metrology configuration's meter roles for a usage point.");

    private final String key;
    private final String defaultFormat;

    MicroCheckTranslationKeys(String key, String defaultFormat) {
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

    public static class Keys {
        static String NAME_PREFIX = "usage.point.micro.check.name.";
        static String MESSAGE_PREFIX = "usage.point.micro.check.message.";

        private Keys() {
        }
    }
}
