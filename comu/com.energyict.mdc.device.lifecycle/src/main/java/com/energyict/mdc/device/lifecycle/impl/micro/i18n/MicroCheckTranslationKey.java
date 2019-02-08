/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.impl.micro.i18n;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;

import java.util.Arrays;
import java.util.Optional;

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
    MICRO_CHECK_NAME_ALL_DATA_COLLECTED(Keys.MICRO_CHECK_NAME_TRANSLATE_KEY + MicroCheck.ALL_LOAD_PROFILE_DATA_COLLECTED, "All load profile data collected"),
    MICRO_CHECK_NAME_ALL_DATA_VALID(Keys.MICRO_CHECK_NAME_TRANSLATE_KEY + MicroCheck.ALL_DATA_VALID, "All data valid"),
    MICRO_CHECK_NAME_SLAVE_DEVICE_HAS_GATEWAY(Keys.MICRO_CHECK_NAME_TRANSLATE_KEY + MicroCheck.SLAVE_DEVICE_HAS_GATEWAY, "Check for master device"),
    MICRO_CHECK_NAME_LINKED_WITH_USAGE_POINT(Keys.MICRO_CHECK_NAME_TRANSLATE_KEY + MicroCheck.LINKED_WITH_USAGE_POINT, "Connected to usage point"),
    MICRO_CHECK_NAME_ALL_ISSUES_AND_ALARMS_ARE_CLOSED(Keys.MICRO_CHECK_NAME_TRANSLATE_KEY + MicroCheck.ALL_ISSUES_AND_ALARMS_ARE_CLOSED, "All issues and alarms closed"),
    MICRO_CHECK_NAME_MANDATORY_COMMUNICATION_ATTRIBUTES_AVAILABLE(Keys.MICRO_CHECK_NAME_TRANSLATE_KEY + "MANDATORY_COMMUNICATION_ATTRIBUTES_AVAILABLE", "Mandatory communication attributes available"),
    MICRO_CHECK_NAME_AT_LEAST_ONE_ACTIVE_CONNECTION_AVAILABLE(Keys.MICRO_CHECK_NAME_TRANSLATE_KEY + MicroCheck.AT_LEAST_ONE_ACTIVE_CONNECTION_AVAILABLE, "At least one active connection available"),
    MICRO_CHECK_NAME_ALL_DATA_VALIDATED(Keys.MICRO_CHECK_NAME_TRANSLATE_KEY + MicroCheck.ALL_DATA_VALIDATED, "All data validated"),
    MICRO_CHECK_NAME_METROLOGY_CONFIGURATION_IN_CORRECT_STATE_IF_ANY(Keys.MICRO_CHECK_NAME_TRANSLATE_KEY + MicroCheck.METROLOGY_CONFIGURATION_IN_CORRECT_STATE_IF_ANY, "This device is linked to an usage point and cannot be moved from its operational life cycle stage. A metrology configuration is active on the usage point at the moment of the life cycle transition."),
    MICRO_CHECK_NAME_NO_LINKED_MULTI_ELEMENT_SLAVES(Keys.MICRO_CHECK_NAME_TRANSLATE_KEY + MicroCheck.NO_LINKED_MULTI_ELEMENT_SLAVES, "No operational slave devices"),
    MICRO_CHECK_NAME_NO_ACTIVE_SERVICE_CALLS(Keys.MICRO_CHECK_NAME_TRANSLATE_KEY + MicroCheck.NO_ACTIVE_SERVICE_CALLS, "No active service calls"),
    MICRO_CHECK_NAME_AT_LEAST_ONE_ZONE_LINKED(Keys.MICRO_CHECK_NAME_TRANSLATE_KEY + MicroCheck.AT_LEAST_ONE_ZONE_LINKED, "Check if the device is linked to a zone"),
    MICRO_CHECK_DESCRIPTION_DEFAULT_CONNECTION_AVAILABLE(Keys.MICRO_CHECK_DESCRIPTION_TRANSLATE_KEY + MicroCheck.DEFAULT_CONNECTION_AVAILABLE, "Check if a default connection is available on the device."),
    MICRO_CHECK_DESCRIPTION_AT_LEAST_ONE_MANUALLY_SCHEDULED_COMMUNICATION_TASK_AVAILABLE(Keys.MICRO_CHECK_DESCRIPTION_TRANSLATE_KEY + MicroCheck.AT_LEAST_ONE_SCHEDULED_COMMUNICATION_TASK_AVAILABLE, "Check if at least one communication task has been scheduled."),
    MICRO_CHECK_DESCRIPTION_AT_LEAST_ONE_SCHEDULED_COMMUNICATION_TASK_AVAILABLE(Keys.MICRO_CHECK_DESCRIPTION_TRANSLATE_KEY + MicroCheck.AT_LEAST_ONE_SHARED_COMMUNICATION_SCHEDULE_AVAILABLE, "Check if at least one shared communication schedule has been added to the device."),
    MICRO_CHECK_DESCRIPTION_ALL_DATA_COLLECTED(Keys.MICRO_CHECK_DESCRIPTION_TRANSLATE_KEY + MicroCheck.ALL_LOAD_PROFILE_DATA_COLLECTED, "Check if all load profile data of this device has been collected."),
    MICRO_CHECK_DESCRIPTION_ALL_DATA_VALID(Keys.MICRO_CHECK_DESCRIPTION_TRANSLATE_KEY + MicroCheck.ALL_DATA_VALID, "Check if all the collected data is valid."),
    MICRO_CHECK_DESCRIPTION_SLAVE_DEVICE_HAS_GATEWAY(Keys.MICRO_CHECK_DESCRIPTION_TRANSLATE_KEY + MicroCheck.SLAVE_DEVICE_HAS_GATEWAY, "If this device is a slave, check if the device has been linked to a master device."),
    MICRO_CHECK_DESCRIPTION_LINKED_WITH_USAGE_POINT(Keys.MICRO_CHECK_DESCRIPTION_TRANSLATE_KEY + MicroCheck.LINKED_WITH_USAGE_POINT, "Check if this device is connected to a usage point."),
    MICRO_CHECK_DESCRIPTION_ALL_ISSUES_AND_ALARMS_ARE_CLOSED(Keys.MICRO_CHECK_DESCRIPTION_TRANSLATE_KEY + MicroCheck.ALL_ISSUES_AND_ALARMS_ARE_CLOSED, "Check if all the issues and alarms on this device are closed."),
    MICRO_CHECK_DESCRIPTION_MANDATORY_COMMUNICATION_ATTRIBUTES_AVAILABLE(Keys.MICRO_CHECK_DESCRIPTION_TRANSLATE_KEY + "MANDATORY_COMMUNICATION_ATTRIBUTES_AVAILABLE", "Check if the mandatory communication attributes are available on the device: protocol dialect attributes, security setting attributes, connection attributes, general attributes."),
    MICRO_CHECK_DESCRIPTION_AT_LEAST_ONE_ACTIVE_CONNECTION_AVAILABLE(Keys.MICRO_CHECK_DESCRIPTION_TRANSLATE_KEY + MicroCheck.AT_LEAST_ONE_ACTIVE_CONNECTION_AVAILABLE, "Check if at least one connection is available on the device with the status: \"Active\"."),
    MICRO_CHECK_DESCRIPTION_ALL_DATA_VALIDATED(Keys.MICRO_CHECK_DESCRIPTION_TRANSLATE_KEY + MicroCheck.ALL_DATA_VALIDATED, "Check if all the collected data is validated."),
    MICRO_CHECK_DESCRIPTION_NO_ACTIVE_SERVICE_CALLS(Keys.MICRO_CHECK_DESCRIPTION_TRANSLATE_KEY + MicroCheck.NO_ACTIVE_SERVICE_CALLS, "Check that no service calls are active for the device."),
    MICRO_CHECK_DESCRIPTION_METROLOGY_CONFIGURATION_IN_CORRECT_STATE_IF_ANY(Keys.MICRO_CHECK_DESCRIPTION_TRANSLATE_KEY + MicroCheck.METROLOGY_CONFIGURATION_IN_CORRECT_STATE_IF_ANY, "Check if the device is linked to a usage point that has an active metrology configuration"),
    MICRO_CHECK_DESCRIPTION_NO_LINKED_MULTI_ELEMENT_SLAVES(Keys.MICRO_CHECK_DESCRIPTION_TRANSLATE_KEY + MicroCheck.NO_LINKED_MULTI_ELEMENT_SLAVES, "Check if there are no multi element slaves in an operational stage"),
    MICRO_CHECK_DESCRIPTION_AT_LEAST_ONE_ZONE_LINKED(Keys.MICRO_CHECK_DESCRIPTION_TRANSLATE_KEY + MicroCheck.AT_LEAST_ONE_ZONE_LINKED, "Check if the device has at least one zone linked"),
    ;

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


    public static Optional<TranslationKey> getNameFor(MicroCheck microCheck){
        return findTranslation(Keys.MICRO_CHECK_NAME_TRANSLATE_KEY + microCheck.name());
    }

    public static Optional<TranslationKey> getDescriptionFor(MicroCheck microCheck){
        return findTranslation(Keys.MICRO_CHECK_DESCRIPTION_TRANSLATE_KEY + microCheck.name());
    }

    private static Optional<TranslationKey> findTranslation(String key){
        return Arrays.stream(MicroCheckTranslationKey.values())
                .filter(candidate -> candidate.getKey().equals(key))
                .map(TranslationKey.class::cast)
                .findFirst();
    }

    public static class Keys {
        public static final String MICRO_CHECK_NAME_TRANSLATE_KEY = "transition.microcheck.name.";
        public static final String MICRO_CHECK_DESCRIPTION_TRANSLATE_KEY = "transition.microcheck.description.";
        private Keys() {}
    }

}