package com.energyict.mdc.device.lifecycle.config.impl;

import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

/**
 * Defines the different error message that are produced by
 * this "device life cycle configuration" bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-11 (11:05)
 */
public enum MessageSeeds implements MessageSeed, TranslationKey {

    // Generic
    FIELD_TOO_LONG(100, Keys.FIELD_TOO_LONG, "Field must not exceed {max} characters"),
    CANNOT_BE_EMPTY(101, Keys.CAN_NOT_BE_EMPTY, "This field is required"),

    // TransitionBusinessProcess
    NO_SUCH_PROCESS(1000, Keys.NO_SUCH_PROCESS, "No external business process with deployment id {0} and process id {1}"),
    TRANSITION_PROCESS_IN_USE(1001, Keys.TRANSITION_PROCESS_IN_USE, "The external business process with deployment id {0} and process id {1} is still in use by at least one transition action"),

    // DeviceLifeCycle
    UNIQUE_DEVICE_LIFE_CYCLE_NAME(202, Keys.UNIQUE_DEVICE_LIFE_CYCLE_NAME, "Name must be unique"),
    MAXIMUM_FUTURE_EFFECTIVE_TIME_SHIFT_NOT_IN_RANGE(203, Keys.MAXIMUM_FUTURE_EFFECTIVE_TIME_SHIFT_NOT_IN_RANGE, "Field must not exceed the maximum value"),
    MAXIMUM_PAST_EFFECTIVE_TIME_SHIFT_NOT_IN_RANGE(204, Keys.MAXIMUM_PAST_EFFECTIVE_TIME_SHIFT_NOT_IN_RANGE, "Field must not exceed the maximum value");

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
        public static final String FIELD_TOO_LONG = "FieldTooLong";
        public static final String CAN_NOT_BE_EMPTY = "CanNotBeEmpty";
        public static final String NO_SUCH_PROCESS = "device.life.cycle.unknown.process";
        public static final String TRANSITION_PROCESS_IN_USE = "device.life.cycle.process.inUse";
        public static final String UNIQUE_DEVICE_LIFE_CYCLE_NAME = "device.life.cycle.unique.name";
        public static final String MAXIMUM_FUTURE_EFFECTIVE_TIME_SHIFT_NOT_IN_RANGE = "maximumFutureEffectiveTimeShiftNotInRange";
        public static final String MAXIMUM_PAST_EFFECTIVE_TIME_SHIFT_NOT_IN_RANGE = "maximumPastEffectiveTimeShiftNotInRange";
    }

}