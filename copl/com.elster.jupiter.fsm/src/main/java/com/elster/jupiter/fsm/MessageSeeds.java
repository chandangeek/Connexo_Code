package com.elster.jupiter.fsm;

import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

/**
 * Defines the different error messages that are produced by
 * this "finate state machine" bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-02 (16:23)
 */
public enum MessageSeeds implements MessageSeed, TranslationKey {

    // Generic
    FIELD_TOO_LONG(100, Keys.FIELD_TOO_LONG, "Field must not exceed {max} characters"),
    CAN_NOT_BE_EMPTY(101, Keys.CAN_NOT_BE_EMPTY, "This field cannot be empty"),

    // StateTransitionEventType
    UNIQUE_EVENT_TYPE_SYMBOL(200, Keys.UNIQUE_EVENT_TYPE_SYMBOL, "The symbolic representation of a state transition event type must be unique"),
    UNIQUE_STANDARD_EVENT_TYPE(201, Keys.UNIQUE_STANDARD_EVENT_TYPE, "A standard event type can only once be enabled for use in finate state machines"),
    EVENT_TYPE_STILL_IN_USE(202, Keys.EVENT_TYPE_STILL_IN_USE, "The standard event type cannot be removed because it is still in use by the following finate state machines: {0}"),

    // State
    UNIQUE_STATE_NAME(300, Keys.UNIQUE_STATE_NAME, "The name of a state must be unique in the finate state machine's context"),
    NO_SUCH_PROCESS(301, Keys.NO_SUCH_PROCESS, "No external business process with deployment id {0} and process id {1} is linked to state {2} of finate state machine {3}"),

    // FinateStateMachine
    UNIQUE_FINATE_STATE_MACHINE_NAME(400, Keys.UNIQUE_FINATE_STATE_MACHINE_NAME, "The name of a finate state machine must be unique"),
    AT_LEAST_ONE_STATE(401, Keys.AT_LEAST_ONE_STATE, "A finate state machine must have at least one state"),
    UNKNOWN_STATE(402, Keys.UNKNOWN_STATE, "Unable to remove state {0} because it does not exist in the finate state machine {1}"),
    UNKNOWN_STATE_TRANSITION(403, Keys.UNKNOWN_STATE_TRANSITION, "Unable to remove state transition from {0} and event type {1} because it does not exist in the finate state machine {2}");

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
        return FinateStateMachineService.COMPONENT_NAME;
    }

    public static final class Keys {
        public static final String FIELD_TOO_LONG = "FieldTooLong";
        public static final String CAN_NOT_BE_EMPTY = "CanNotBeEmpty";
        public static final String UNIQUE_EVENT_TYPE_SYMBOL = "state.transition.event.type.unique.symbol";
        public static final String UNIQUE_STANDARD_EVENT_TYPE = "state.transition.event.type.unique.standard";
        public static final String EVENT_TYPE_STILL_IN_USE = "state.transition.event.type.inUse";
        public static final String UNIQUE_STATE_NAME = "state.unique.name";
        public static final String UNIQUE_FINATE_STATE_MACHINE_NAME = "finate.state.machine.unique.name";
        public static final String AT_LEAST_ONE_STATE = "finate.state.machine.min1state";
        public static final String UNKNOWN_STATE = "finate.state.machine.unknown.state";
        public static final String UNKNOWN_STATE_TRANSITION = "finate.state.machine.unknown.state.transition";
        public static final String NO_SUCH_PROCESS = "finate.state.machine.unknown.state.process";
    }

}