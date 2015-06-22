package com.energyict.mdc.device.lifecycle.config.rest.i18n;

import com.energyict.mdc.device.lifecycle.config.MicroAction;

import com.elster.jupiter.nls.TranslationKey;

/**
 * Contains translation keys for the {@link com.energyict.mdc.device.lifecycle.config.MicroAction}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-06-22 (11:11)
 */
public enum MicroActionTranslationKey implements TranslationKey {

    MICRO_ACTION_NAME_SET_LAST_READING(Keys.MICRO_ACTION_NAME_TRANSLATE_KEY + MicroAction.SET_LAST_READING, "Input last meter reading"),
    MICRO_ACTION_NAME_ENABLE_VALIDATION(Keys.MICRO_ACTION_NAME_TRANSLATE_KEY + MicroAction.ENABLE_VALIDATION, "Activate validation"),
    MICRO_ACTION_NAME_DISABLE_VALIDATION(Keys.MICRO_ACTION_NAME_TRANSLATE_KEY + MicroAction.DISABLE_VALIDATION, "Deactivate validation"),
    MICRO_ACTION_NAME_ACTIVATE_CONNECTION_TASKS(Keys.MICRO_ACTION_NAME_TRANSLATE_KEY + MicroAction.ACTIVATE_CONNECTION_TASKS, "Activate connections in use"),
    MICRO_ACTION_NAME_START_COMMUNICATION(Keys.MICRO_ACTION_NAME_TRANSLATE_KEY + MicroAction.START_COMMUNICATION, "Trigger all communication"),
    MICRO_ACTION_NAME_DISABLE_COMMUNICATION(Keys.MICRO_ACTION_NAME_TRANSLATE_KEY + MicroAction.DISABLE_COMMUNICATION, "Deactivate communication"),
    MICRO_ACTION_NAME_CREATE_METER_ACTIVATION(Keys.MICRO_ACTION_NAME_TRANSLATE_KEY + MicroAction.CREATE_METER_ACTIVATION, "Create new meter activation"),
    MICRO_ACTION_NAME_CLOSE_METER_ACTIVATION(Keys.MICRO_ACTION_NAME_TRANSLATE_KEY + MicroAction.CLOSE_METER_ACTIVATION, "Stop meter activation"),
    MICRO_ACTION_NAME_REMOVE_DEVICE_FROM_STATIC_GROUPS(Keys.MICRO_ACTION_NAME_TRANSLATE_KEY + MicroAction.REMOVE_DEVICE_FROM_STATIC_GROUPS, "Remove device from static groups"),
    MICRO_ACTION_NAME_DETACH_SLAVE_FROM_MASTER(Keys.MICRO_ACTION_NAME_TRANSLATE_KEY + MicroAction.DETACH_SLAVE_FROM_MASTER, "Disconnect slave from master"),

    MICRO_ACTION_DESCRIPTION_SET_LAST_READING(Keys.MICRO_ACTION_DESCRIPTION_TRANSLATE_KEY + MicroAction.SET_LAST_READING, "Provide the last meter reading manually based on one or more register group."),
    MICRO_ACTION_DESCRIPTION_ENABLE_VALIDATION(Keys.MICRO_ACTION_DESCRIPTION_TRANSLATE_KEY + MicroAction.ENABLE_VALIDATION, "Activate the data validation on this device. This auto action is effective immediately."),
    MICRO_ACTION_DESCRIPTION_DISABLE_VALIDATION(Keys.MICRO_ACTION_DESCRIPTION_TRANSLATE_KEY + MicroAction.DISABLE_VALIDATION, "Stop the data validation on this device."),
    MICRO_ACTION_DESCRIPTION_ACTIVATE_CONNECTION_TASKS(Keys.MICRO_ACTION_DESCRIPTION_TRANSLATE_KEY + MicroAction.ACTIVATE_CONNECTION_TASKS, "Activate inactive connections used in scheduled communication tasks."),
    MICRO_ACTION_DESCRIPTION_START_COMMUNICATION(Keys.MICRO_ACTION_DESCRIPTION_TRANSLATE_KEY + MicroAction.START_COMMUNICATION, "Trigger all the recurring and non-recurring communication tasks and their connections on the date of the transition."),
    MICRO_ACTION_DESCRIPTION_DISABLE_COMMUNICATION(Keys.MICRO_ACTION_DESCRIPTION_TRANSLATE_KEY + MicroAction.DISABLE_COMMUNICATION, "Deactivate the connections and communication tasks on this device."),
    MICRO_ACTION_DESCRIPTION_CREATE_METER_ACTIVATION(Keys.MICRO_ACTION_DESCRIPTION_TRANSLATE_KEY + MicroAction.CREATE_METER_ACTIVATION, "Create a new meter activation on the transition date."),
    MICRO_ACTION_DESCRIPTION_CLOSE_METER_ACTIVATION(Keys.MICRO_ACTION_DESCRIPTION_TRANSLATE_KEY + MicroAction.CLOSE_METER_ACTIVATION, "Stop the meter activation of this device and unlink the device from its usage point."),
    MICRO_ACTION_DESCRIPTION_REMOVE_DEVICE_FROM_STATIC_GROUPS(Keys.MICRO_ACTION_DESCRIPTION_TRANSLATE_KEY + MicroAction.REMOVE_DEVICE_FROM_STATIC_GROUPS, "Remove this device from the static device groups."),
    MICRO_ACTION_DESCRIPTION_DETACH_SLAVE_FROM_MASTER(Keys.MICRO_ACTION_DESCRIPTION_TRANSLATE_KEY + MicroAction.DETACH_SLAVE_FROM_MASTER, "If this device is a slave, disconnect it from his master.");

    private final String key;
    private final String defaultFormat;

    MicroActionTranslationKey(String key, String defaultFormat) {
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
        public static final String MICRO_ACTION_NAME_TRANSLATE_KEY = "transition.microaction.name.";
        public static final String MICRO_ACTION_DESCRIPTION_TRANSLATE_KEY = "transition.microaction.description.";
        private Keys() {}
    }

}