package com.energyict.mdc.device.lifecycle.impl;

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
    NOT_ALLOWED_2_EXECUTE(102, Keys.NOT_ALLOWED_2_EXECUTE, "The current user is not allowed to execute this action");

    private final int number;
    private final String key;
    private final String defaultFormat;
    private final Level level;

    MessageSeeds(int number, String key, String defaultFormat) {
        this(number, key, defaultFormat, Level.SEVERE);
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
    }

}