package com.energyict.mdc.device.lifecycle.config.rest.i18n;

import com.energyict.mdc.device.lifecycle.config.MicroCheck;
import com.energyict.mdc.device.lifecycle.config.rest.info.MicroActionAndCheckInfoFactory;

import com.elster.jupiter.nls.TranslationKey;

/**
 * Contains translation keys for the {@link com.energyict.mdc.device.lifecycle.config.MicroCheck}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-06-22 (11:17)
 */
public enum MicroCheckTranslationKey implements TranslationKey {

    MICRO_CHECK_NAME_DEFAULT_CONNECTION_AVAILABLE(Keys.MICRO_CHECK_NAME_TRANSLATE_KEY + MicroCheck.DEFAULT_CONNECTION_AVAILABLE, "Default connection available"),
    MICRO_CHECK_NAME_AT_LEAST_ONE_MANUALLY_SCHEDULED_COMMUNICATION_TASK_AVAILABLE(Keys.MICRO_CHECK_NAME_TRANSLATE_KEY + MicroCheck.AT_LEAST_ONE_SCHEDULED_COMMUNICATION_TASK_AVAILABLE, "At least one scheduled communication task"),
    MICRO_CHECK_NAME_AT_LEAST_ONE_SCHEDULED_COMMUNICATION_TASK_AVAILABLE(Keys.MICRO_CHECK_NAME_TRANSLATE_KEY + MicroCheck.AT_LEAST_ONE_SHARED_COMMUNICATION_SCHEDULE_AVAILABLE, "At least one shared communication schedule"),
    MICRO_CHECK_NAME_ALL_DATA_COLLECTED(Keys.MICRO_CHECK_NAME_TRANSLATE_KEY + MicroCheck.ALL_LOAD_PROFILE_DATA_COLLECTED, "All data collected"),
    MICRO_CHECK_NAME_ALL_DATA_VALID(Keys.MICRO_CHECK_NAME_TRANSLATE_KEY + MicroCheck.ALL_DATA_VALID, "All data valid"),
    MICRO_CHECK_NAME_SLAVE_DEVICE_HAS_GATEWAY(Keys.MICRO_CHECK_NAME_TRANSLATE_KEY + MicroCheck.SLAVE_DEVICE_HAS_GATEWAY, "Check for master device"),
    MICRO_CHECK_NAME_LINKED_WITH_USAGE_POINT(Keys.MICRO_CHECK_NAME_TRANSLATE_KEY + MicroCheck.LINKED_WITH_USAGE_POINT, "Connected to usage point"),
    MICRO_CHECK_NAME_ALL_ISSUES_AND_ALARMS_ARE_CLOSED(Keys.MICRO_CHECK_NAME_TRANSLATE_KEY + MicroCheck.ALL_ISSUES_AND_ALARMS_ARE_CLOSED, "All issues closed"),
    MICRO_CHECK_NAME_MANDATORY_COMMUNICATION_ATTRIBUTES_AVAILABLE(Keys.MICRO_CHECK_NAME_TRANSLATE_KEY + MicroActionAndCheckInfoFactory.CONSOLIDATED_MICRO_CHECKS_KEY, "Mandatory communication attributes available"),

    MICRO_CHECK_DESCRIPTION_DEFAULT_CONNECTION_AVAILABLE(Keys.MICRO_CHECK_DESCRIPTION_TRANSLATE_KEY + MicroCheck.DEFAULT_CONNECTION_AVAILABLE, "Check if a default connection is available on the device."),
    MICRO_CHECK_DESCRIPTION_AT_LEAST_ONE_MANUALLY_SCHEDULED_COMMUNICATION_TASK_AVAILABLE(Keys.MICRO_CHECK_DESCRIPTION_TRANSLATE_KEY + MicroCheck.AT_LEAST_ONE_SCHEDULED_COMMUNICATION_TASK_AVAILABLE, "Check if at least one communication task has been scheduled."),
    MICRO_CHECK_DESCRIPTION_AT_LEAST_ONE_SCHEDULED_COMMUNICATION_TASK_AVAILABLE(Keys.MICRO_CHECK_DESCRIPTION_TRANSLATE_KEY + MicroCheck.AT_LEAST_ONE_SHARED_COMMUNICATION_SCHEDULE_AVAILABLE, "Check if at least one shared communication schedule has been added to the device."),
    MICRO_CHECK_DESCRIPTION_ALL_DATA_COLLECTED(Keys.MICRO_CHECK_DESCRIPTION_TRANSLATE_KEY + MicroCheck.ALL_LOAD_PROFILE_DATA_COLLECTED, "Check if all the data of this device has been collected."),
    MICRO_CHECK_DESCRIPTION_ALL_DATA_VALID(Keys.MICRO_CHECK_DESCRIPTION_TRANSLATE_KEY + MicroCheck.ALL_DATA_VALID, "Check if all the collected data is valid."),
    MICRO_CHECK_DESCRIPTION_SLAVE_DEVICE_HAS_GATEWAY(Keys.MICRO_CHECK_DESCRIPTION_TRANSLATE_KEY + MicroCheck.SLAVE_DEVICE_HAS_GATEWAY, "If this device is a slave, check if the device has been linked to a master device."),
    MICRO_CHECK_DESCRIPTION_LINKED_WITH_USAGE_POINT(Keys.MICRO_CHECK_DESCRIPTION_TRANSLATE_KEY + MicroCheck.LINKED_WITH_USAGE_POINT, "Check if this device is connected to a usage point."),
    MICRO_CHECK_DESCRIPTION_ALL_ISSUES_AND_ALARMS_ARE_CLOSED(Keys.MICRO_CHECK_DESCRIPTION_TRANSLATE_KEY + MicroCheck.ALL_ISSUES_AND_ALARMS_ARE_CLOSED, "Check if all the issues on this device are closed."),
    MICRO_CHECK_DESCRIPTION_MANDATORY_COMMUNICATION_ATTRIBUTES_AVAILABLE(Keys.MICRO_CHECK_DESCRIPTION_TRANSLATE_KEY + MicroActionAndCheckInfoFactory.CONSOLIDATED_MICRO_CHECKS_KEY, "Check if the mandatory communication attributes are available on the device: protocol dialect attributes, security setting attributes, connection attributes, general attributes.");

    private final String key;
    private final String defaultFormat;

    MicroCheckTranslationKey(String key, String defaultFormat) {
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

    public static class Keys {
        public static final String MICRO_CHECK_NAME_TRANSLATE_KEY = "transition.microcheck.name.";
        public static final String MICRO_CHECK_DESCRIPTION_TRANSLATE_KEY = "transition.microcheck.description.";
        private Keys() {}
    }

}