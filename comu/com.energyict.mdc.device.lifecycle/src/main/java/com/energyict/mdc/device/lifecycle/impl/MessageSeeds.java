package com.energyict.mdc.device.lifecycle.impl;

import com.energyict.mdc.device.lifecycle.config.MicroCheck;

import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

/**
 * Defines the different error message that are produced by
 * this "device life cycle" bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-20 (16:29)
 */
public enum MessageSeeds implements MessageSeed, TranslationKey {

    TRANSITION_ACTION_SOURCE_IS_NOT_CURRENT_STATE(100, Keys.TRANSITION_ACTION_SOURCE_IS_NOT_CURRENT_STATE, "Action '{0}' cannot be executed against the device (id={1}) because the device's life cycle does not support that action in its current state"),
    BPM_ACTION_SOURCE_IS_NOT_CURRENT_STATE(101, Keys.BPM_ACTION_SOURCE_IS_NOT_CURRENT_STATE, "Business process with deployment id '{0}' and process id '{1}' cannot be executed against the device (id={2}) because the device's life cycle does not support that action in its current state"),
    NOT_ALLOWED_2_EXECUTE(102, Keys.NOT_ALLOWED_2_EXECUTE, "The current user is not allowed to execute this action"),

    // MicroChecks
    MULTIPLE_MICRO_CHECKS_FAILED(10000, Keys.MULTIPLE_MICRO_CHECKS_FAILED, "Action cannot be triggered because the following checks have failed: {0}"),
    DEFAULT_CONNECTION_AVAILABLE(10001, MicroCheck.DEFAULT_CONNECTION_AVAILABLE, "There should at least be a default connection task"),
    AT_LEAST_ONE_MANUALLY_SCHEDULED_COMMUNICATION_TASK_AVAILABLE(10002, MicroCheck.AT_LEAST_ONE_MANUALLY_SCHEDULED_COMMUNICATION_TASK_AVAILABLE, "At least one communication task has been scheduled"),
    ALL_DATA_COLLECTED(10003, MicroCheck.ALL_DATA_COLLECTED, "All the data on the device must have been collected"),
    GENERAL_PROTOCOL_PROPERTIES_ARE_ALL_VALID(10004, MicroCheck.GENERAL_PROTOCOL_PROPERTIES_ARE_ALL_VALID, "All mandatory general protocol properties should be valid and specified"),
    PROTOCOL_DIALECT_PROPERTIES_ARE_ALL_VALID(10005, MicroCheck.PROTOCOL_DIALECT_PROPERTIES_ARE_ALL_VALID, "All mandatory protocol dialect properties should be valid and specified"),
    SECURITY_PROPERTIES_ARE_ALL_VALID(10006, MicroCheck.SECURITY_PROPERTIES_ARE_ALL_VALID, "All mandatory security properties should be valid and specified"),
    CONNECTION_PROPERTIES_ARE_ALL_VALID(10008, MicroCheck.CONNECTION_PROPERTIES_ARE_ALL_VALID, "All mandatory connection method properties should be valid and specified"),
    SLAVE_DEVICE_HAS_GATEWAY(10009, MicroCheck.SLAVE_DEVICE_HAS_GATEWAY, "A slave device must have a gateway device"),
    LINKED_WITH_USAGE_POINT(10010, MicroCheck.LINKED_WITH_USAGE_POINT, "A device must be linked to a usage point"),
    ALL_ISSUES_AND_ALARMS_ARE_CLOSED(10011, MicroCheck.ALL_ISSUES_AND_ALARMS_ARE_CLOSED, "All issues and alarms must have been closed or resolved on the device"),
    AT_LEAST_ONE_SCHEDULED_COMMUNICATION_TASK_AVAILABLE(10012, MicroCheck.AT_LEAST_ONE_SCHEDULED_COMMUNICATION_TASK_AVAILABLE, "At least one shared communication schedule has added to the device"),
    ALL_DATA_VALID(10013, MicroCheck.ALL_DATA_VALID, "All the collected data on the device must be valid"),

    // MicroActions
    MISSING_REQUIRED_PROPERTY_VALUES(20001, Keys.MISSING_REQUIRED_PROPERTY_VALUES, "No value was specified for the following property spec of the configured actions: {0}")
    ;

    private final int number;
    private final String key;
    private final String defaultFormat;
    private final Level level;

    MessageSeeds(int number, String key, String defaultFormat) {
        this(number, key, defaultFormat, Level.SEVERE);
    }

    MessageSeeds(int number, MicroCheck microCheck, String defaultFormat) {
        this(number, "mdc.device.lifecycle.micro.action." + microCheck.name(), defaultFormat, Level.INFO);
    }

    MessageSeeds(int number, String key, String defaultFormat, Level level) {
        this.number = number;
        this.key = key;
        this.defaultFormat = defaultFormat;
        this.level = level;
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
        return level;
    }

    @Override
    public String getModule() {
        return FiniteStateMachineService.COMPONENT_NAME;
    }

    public static final class Keys {
        public static final String TRANSITION_ACTION_SOURCE_IS_NOT_CURRENT_STATE = "transitionAction.not.device.current.state";
        public static final String BPM_ACTION_SOURCE_IS_NOT_CURRENT_STATE = "bpmAction.not.device.current.state";
        public static final String NOT_ALLOWED_2_EXECUTE = "authorizedAction.notAllowed2Execute";
        public static final String MULTIPLE_MICRO_CHECKS_FAILED = "authorizedAction.multiple.microChecksFailed";
        public static final String MISSING_REQUIRED_PROPERTY_VALUES = "authorizedAction.microAction.required.properties.multipleMissing";
    }

}