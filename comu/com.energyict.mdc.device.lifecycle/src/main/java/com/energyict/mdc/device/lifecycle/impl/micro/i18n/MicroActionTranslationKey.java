package com.energyict.mdc.device.lifecycle.impl.micro.i18n;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.device.lifecycle.config.MicroAction;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;

import java.util.Arrays;
import java.util.Optional;

/**
 * Contains translation keys for the {@link com.energyict.mdc.device.lifecycle.config.MicroAction}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-06-22 (11:11)
 */
public enum MicroActionTranslationKey implements TranslationKey {

    MICRO_ACTION_NAME_SET_LAST_READING(Keys.MICRO_ACTION_NAME_TRANSLATE_KEY + MicroAction.SET_LAST_READING, "Prepare for data collection"),
    MICRO_ACTION_NAME_ENABLE_VALIDATION(Keys.MICRO_ACTION_NAME_TRANSLATE_KEY + MicroAction.ENABLE_VALIDATION, "Activate validation"),
    MICRO_ACTION_NAME_DISABLE_VALIDATION(Keys.MICRO_ACTION_NAME_TRANSLATE_KEY + MicroAction.DISABLE_VALIDATION, "Deactivate validation"),
    MICRO_ACTION_NAME_ACTIVATE_CONNECTION_TASKS(Keys.MICRO_ACTION_NAME_TRANSLATE_KEY + MicroAction.ACTIVATE_CONNECTION_TASKS_IN_USE, "Activate connections in use"),
    MICRO_ACTION_NAME_START_COMMUNICATION(Keys.MICRO_ACTION_NAME_TRANSLATE_KEY + MicroAction.START_COMMUNICATION, "Trigger all communication"),
    MICRO_ACTION_NAME_DISABLE_COMMUNICATION(Keys.MICRO_ACTION_NAME_TRANSLATE_KEY + MicroAction.DISABLE_COMMUNICATION, "Deactivate communication"),
    MICRO_ACTION_NAME_CREATE_METER_ACTIVATION(Keys.MICRO_ACTION_NAME_TRANSLATE_KEY + MicroAction.CREATE_METER_ACTIVATION, "Create new meter activation"),
    MICRO_ACTION_NAME_CLOSE_METER_ACTIVATION(Keys.MICRO_ACTION_NAME_TRANSLATE_KEY + MicroAction.CLOSE_METER_ACTIVATION, "Stop meter activation"),
    MICRO_ACTION_NAME_REMOVE_DEVICE_FROM_STATIC_GROUPS(Keys.MICRO_ACTION_NAME_TRANSLATE_KEY + MicroAction.REMOVE_DEVICE_FROM_STATIC_GROUPS, "Remove device from static groups"),
    MICRO_ACTION_NAME_DETACH_SLAVE_FROM_MASTER(Keys.MICRO_ACTION_NAME_TRANSLATE_KEY + MicroAction.DETACH_SLAVE_FROM_MASTER, "Disconnect slave from master"),
    MICRO_ACTION_NAME_REMOVE_DEVICE(Keys.MICRO_ACTION_NAME_TRANSLATE_KEY + MicroAction.REMOVE_DEVICE, "Remove device"),
    MICRO_ACTION_NAME_CLOSE_ALL_ISSUES(Keys.MICRO_ACTION_NAME_TRANSLATE_KEY + MicroAction.CLOSE_ALL_ISSUES, "Close all issues"),
    MICRO_ACTION_NAME_START_RECURRING_COMMUNICATION(Keys.MICRO_ACTION_NAME_TRANSLATE_KEY + MicroAction.START_RECURRING_COMMUNICATION, "Trigger recurring communication"),
    MICRO_ACTION_NAME_FORCE_VALIDATION_AND_ESTIMATION(Keys.MICRO_ACTION_NAME_TRANSLATE_KEY + MicroAction.FORCE_VALIDATION_AND_ESTIMATION, "Force validation and estimation"),
    MICRO_ACTION_NAME_FORCE_ENABLE_ESTIMATION(Keys.MICRO_ACTION_NAME_TRANSLATE_KEY + MicroAction.ENABLE_ESTIMATION, "Activate estimation"),
    MICRO_ACTION_NAME_FORCE_DISABLE_ESTIMATION(Keys.MICRO_ACTION_NAME_TRANSLATE_KEY + MicroAction.DISABLE_ESTIMATION, "Deactivate estimation"),

    MICRO_ACTION_DESCRIPTION_SET_LAST_READING(Keys.MICRO_ACTION_DESCRIPTION_TRANSLATE_KEY + MicroAction.SET_LAST_READING, "Set the last reading date, so data can be collected starting from the transition date."),
    MICRO_ACTION_DESCRIPTION_ENABLE_VALIDATION(Keys.MICRO_ACTION_DESCRIPTION_TRANSLATE_KEY + MicroAction.ENABLE_VALIDATION, "Activate the data validation on this device. This auto action is effective immediately."),
    MICRO_ACTION_DESCRIPTION_DISABLE_VALIDATION(Keys.MICRO_ACTION_DESCRIPTION_TRANSLATE_KEY + MicroAction.DISABLE_VALIDATION, "Deactivate the data validation on this device. This auto action is effective immediately."),
    MICRO_ACTION_DESCRIPTION_ACTIVATE_CONNECTION_TASKS(Keys.MICRO_ACTION_DESCRIPTION_TRANSLATE_KEY + MicroAction.ACTIVATE_CONNECTION_TASKS_IN_USE, "Activate inactive connections used in scheduled communication tasks."),
    MICRO_ACTION_DESCRIPTION_START_COMMUNICATION(Keys.MICRO_ACTION_DESCRIPTION_TRANSLATE_KEY + MicroAction.START_COMMUNICATION, "Trigger all the recurring and non-recurring communication tasks and their connections on the date of the transition."),
    MICRO_ACTION_DESCRIPTION_DISABLE_COMMUNICATION(Keys.MICRO_ACTION_DESCRIPTION_TRANSLATE_KEY + MicroAction.DISABLE_COMMUNICATION, "Deactivate the connections and communication tasks on this device."),
    MICRO_ACTION_DESCRIPTION_CREATE_METER_ACTIVATION(Keys.MICRO_ACTION_DESCRIPTION_TRANSLATE_KEY + MicroAction.CREATE_METER_ACTIVATION, "Create a new meter activation on the transition date."),
    MICRO_ACTION_DESCRIPTION_CLOSE_METER_ACTIVATION(Keys.MICRO_ACTION_DESCRIPTION_TRANSLATE_KEY + MicroAction.CLOSE_METER_ACTIVATION, "Stop the meter activation of this device and unlink the device from its usage point."),
    MICRO_ACTION_DESCRIPTION_REMOVE_DEVICE_FROM_STATIC_GROUPS(Keys.MICRO_ACTION_DESCRIPTION_TRANSLATE_KEY + MicroAction.REMOVE_DEVICE_FROM_STATIC_GROUPS, "Remove this device from the static device groups."),
    MICRO_ACTION_DESCRIPTION_DETACH_SLAVE_FROM_MASTER(Keys.MICRO_ACTION_DESCRIPTION_TRANSLATE_KEY + MicroAction.DETACH_SLAVE_FROM_MASTER, "If this device is a slave, disconnect it from his master."),
    MICRO_ACTION_DESCRIPTION_REMOVE_DEVICE(Keys.MICRO_ACTION_DESCRIPTION_TRANSLATE_KEY + MicroAction.REMOVE_DEVICE, "\tRemove the device and all of its data, and close the current meter activation."),
    MICRO_ACTION_DESCRIPTION_CLOSE_ALL_ISSUES(Keys.MICRO_ACTION_DESCRIPTION_TRANSLATE_KEY + MicroAction.CLOSE_ALL_ISSUES, "Close the communication and validation issues on the device with the status \"Won't fix\"."),
    MICRO_ACTION_DESCRIPTION_START_RECURRING_COMMUNICATION(Keys.MICRO_ACTION_DESCRIPTION_TRANSLATE_KEY + MicroAction.START_RECURRING_COMMUNICATION, "Trigger all the recurring communication tasks and their connections on the date of the transition."),
    MICRO_ACTION_DESCRIPTION_FORCE_VALIDATION_AND_ESTIMATION(Keys.MICRO_ACTION_DESCRIPTION_TRANSLATE_KEY + MicroAction.FORCE_VALIDATION_AND_ESTIMATION, "Force the validation and the estimation to resolve all reading quality issues."),
    MICRO_ACTION_DESCRIPTION_ENABLE_ESTIMATION(Keys.MICRO_ACTION_DESCRIPTION_TRANSLATE_KEY + MicroAction.ENABLE_ESTIMATION, "Activate the data estimation on this device. This auto change is effective immediately."),
    MICRO_ACTION_DESCRIPTION_DISABLE_ESTIMATION(Keys.MICRO_ACTION_DESCRIPTION_TRANSLATE_KEY + MicroAction.DISABLE_ESTIMATION, "Stop the data estimation on this device."),
    ;

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

    public static Optional<TranslationKey> getNameFor(MicroAction microAction){
        return findTranslation(Keys.MICRO_ACTION_NAME_TRANSLATE_KEY + microAction.name());
    }

    public static Optional<TranslationKey> getDescriptionFor(MicroAction microAction){
        return findTranslation(Keys.MICRO_ACTION_DESCRIPTION_TRANSLATE_KEY + microAction.name());
    }

    private static Optional<TranslationKey> findTranslation(String key){
        return Arrays.stream(MicroActionTranslationKey.values())
                .filter(candidate -> candidate.getKey().equals(key))
                .map(TranslationKey.class::cast)
                .findFirst();
    }

    public static class Keys {
        public static final String MICRO_ACTION_NAME_TRANSLATE_KEY = "transition.microaction.name.";
        public static final String MICRO_ACTION_DESCRIPTION_TRANSLATE_KEY = "transition.microaction.description.";
        private Keys() {}
    }

}