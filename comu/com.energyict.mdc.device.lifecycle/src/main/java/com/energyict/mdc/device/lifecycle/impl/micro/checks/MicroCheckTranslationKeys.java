/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.elster.jupiter.nls.TranslationKey;

public enum MicroCheckTranslationKeys implements TranslationKey {

    MICRO_CHECK_NAME_AT_LEAST_ONE_ACTIVE_CONNECTION_AVAILABLE(Keys.NAME_PREFIX + ActiveConnectionAvailable.class.getSimpleName(), "At least one active connection available"),
    MICRO_CHECK_NAME_ALL_DATA_VALID(Keys.NAME_PREFIX + AllDataValid.class.getSimpleName(), "All data valid"),
    MICRO_CHECK_NAME_ALL_DATA_VALIDATED(Keys.NAME_PREFIX + AllDataValidated.class.getSimpleName(), "All data validated"),
    MICRO_CHECK_NAME_ALL_ISSUES_AND_ALARMS_ARE_CLOSED(Keys.NAME_PREFIX + AllIssuesAreClosed.class.getSimpleName(), "All issues and alarms closed"),
    MICRO_CHECK_NAME_ALL_DATA_COLLECTED(Keys.NAME_PREFIX + AllLoadProfileDataCollected.class.getSimpleName(), "All load profile data collected"),

    MICRO_CHECK_MESSAGE_AT_LEAST_ONE_ACTIVE_CONNECTION_AVAILABLE(Keys.MESSAGE_PREFIX + ActiveConnectionAvailable.class.getSimpleName(), "There should at least one active connection on the device"),
    MICRO_CHECK_MESSAGE_ALL_DATA_VALID(Keys.MESSAGE_PREFIX + AllDataValid.class.getSimpleName(), "All the collected data on the device must be valid"),
    MICRO_CHECK_MESSAGE_ALL_DATA_VALIDATED(Keys.MESSAGE_PREFIX + AllDataValidated.class.getSimpleName(), "All the collected data on the device is validated"),
    MICRO_CHECK_MESSAGE_ALL_ISSUES_AND_ALARMS_ARE_CLOSED(Keys.MESSAGE_PREFIX + AllIssuesAreClosed.class.getSimpleName(), "All issues and alarms must have been closed or resolved on the device"),
    MICRO_CHECK_MESSAGE_ALL_LOAD_PROFILE_DATA_COLLECTED(Keys.MESSAGE_PREFIX + AllLoadProfileDataCollected.class.getSimpleName(), "All the data on the device must have been collected"),

    MICRO_CHECK_DESCRIPTION_AT_LEAST_ONE_ACTIVE_CONNECTION_AVAILABLE(Keys.DESCRIPTION_PREFIX + ActiveConnectionAvailable.class.getSimpleName(), "Check if at least one connection is available on the device with the status: 'Active'"),
    MICRO_CHECK_DESCRIPTION_ALL_DATA_VALID(Keys.DESCRIPTION_PREFIX + AllDataValid.class.getSimpleName(), "Check if all the collected data is valid"),
    MICRO_CHECK_DESCRIPTION_ALL_DATA_VALIDATED(Keys.DESCRIPTION_PREFIX + AllDataValidated.class.getSimpleName(), "Check if all the collected data is validated."),
    MICRO_CHECK_DESCRIPTION_ALL_ISSUES_AND_ALARMS_ARE_CLOSED(Keys.DESCRIPTION_PREFIX + AllIssuesAreClosed.class.getSimpleName(), "Check if all the issues and alarms on this device are closed."),
    MICRO_CHECK_DESCRIPTION_ALL_DATA_COLLECTED(Keys.DESCRIPTION_PREFIX + AllLoadProfileDataCollected.class.getSimpleName(), "Check if all load profile data of this device has been collected."),

    ;

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

        static String NAME_PREFIX = "transition.micro.check.name.";
        static String MESSAGE_PREFIX = "transition.micro.check.message.";
        static String DESCRIPTION_PREFIX = "transition.micro.check.description.";

        private Keys() {
        }
    }
}