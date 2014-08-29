package com.energyict.mdc.scheduling.model.impl;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.scheduling.SchedulingService;
import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {
    SHOULD_BE_AT_LEAST(1, Keys.VALUE_TOO_SMALL, "Minimal acceptable value is {min}, was {value}", Level.SEVERE),
    CAN_NOT_BE_EMPTY(2, Keys.CAN_NOT_BE_EMPTY, "This field can not be empty", Level.SEVERE),
    VALUE_NOT_IN_RANGE(3, Keys.VALUE_NOT_IN_RANGE, "{value} not in range {min} to {max}", Level.SEVERE),
    INVALID_URL(4, Keys.INVALID_URL, "{value} is not a valid URL", Level.SEVERE),
    INVALID_CHARS(5, Keys.INVALID_CHARS, "This field contains invalid chars, should obey {regex}", Level.SEVERE),
    REQUIRED_FOR_HTTPS(6, Keys.CAN_NOT_BE_EMPTY_IF_HTTPS, "This field is mandatory in case https is chosen", Level.SEVERE),
    NOT_UNIQUE(7, Keys.NOT_UNIQUE, "field should be unique", Level.SEVERE),
    NEXT_EXECUTION_SPEC_OFFSET_IS_GREATER_THAN_FREQUENCY(8, Keys.NEXT_EXECUTION_SPEC_OFFSET_IS_GREATER_THAN_FREQUENCY_KEY, "Next Execution Spec's offset is greater than its frequency.", Level.SEVERE),
    NEXT_EXECUTION_SPECS_TEMPORAL_EXPRESSION_REQUIRED(9, Keys.NEXT_EXECUTION_SPECS_TEMPORAL_EXPRESSION_REQUIRED_KEY, "The temporal expression of a NextExecutionSpec is required", Level.SEVERE),
    TEMPORAL_EXPRESSION_FREQUENCY_REQUIRED(10, Keys.TEMPORAL_EXPRESSION_FREQUENCY_REQUIRED_KEY, "The frequency of a temporal expression is required", Level.SEVERE),
    TEMPORAL_EXPRESSION_UNKNOWN_UNIT(11, Keys.TEMPORAL_EXPRESSION_UNKNOWN_UNIT_KEY, "The unit {0} is unknown or unsupported for temporal expressions", Level.SEVERE),
    TEMPORAL_EXPRESSION_FREQUENCY_MUST_BE_STRICTLY_POSITIVE(12, Keys.TEMPORAL_EXPRESSION_FREQUENCY_MUST_BE_STRICTLY_POSITIVE_KEY, "The frequency value of a temporal expression must be a strictly positive number", Level.SEVERE),
    TEMPORAL_EXPRESSION_OFFSET_MUST_BE_POSITIVE(13, Keys.TEMPORAL_EXPRESSION_OFFSET_MUST_BE_POSITIVE_KEY, "The offset value of a temporal expression must be a positive number", Level.SEVERE),
    TOO_LONG(14, Keys.TOO_LONG, "Must be less then {max} characters", Level.SEVERE),
    CANNOT_ADD_COM_TASK_TO_COMSCHEDULE_THAT_IS_IN_USE(15, Keys.CANNOT_ADD_COM_TASK_TO_COMSCHEDULE_THAT_IS_IN_USE, "ComTasks cannot be added to communication schedules that are already linked to devices", Level.SEVERE),
    COM_TASK_USAGES_NOT_FOUND(16, Keys.COM_TASK_USAGES_NOT_FOUND, "Communication schedule should have at least one communication task", Level.SEVERE);

    private final int number;
    private final String key;
    private final String defaultFormat;
    private final Level level;

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
        return SchedulingService.COMPONENT_NAME;
    }

    static final class Keys {

        public static final String VALUE_TOO_SMALL = "ValueTooSmall";
        public static final String CAN_NOT_BE_EMPTY = "CanNotBeEmpty";
        public static final String VALUE_NOT_IN_RANGE = "ValueNotInRange";
        public static final String INVALID_URL = "InvalidURL";
        public static final String INVALID_CHARS = "InvalidChars";
        public static final String CAN_NOT_BE_EMPTY_IF_HTTPS = "CanNotBeEmptyIfHttps";
        public static final String NEXT_EXECUTION_SPEC_OFFSET_IS_GREATER_THAN_FREQUENCY_KEY = "nextExecutionSpecs.offsetGreaterThanFrequency";
        public static final String NEXT_EXECUTION_SPECS_TEMPORAL_EXPRESSION_REQUIRED_KEY = "nextExecutionSpecs.temporalExpression.required";
        public static final String NOT_UNIQUE = "notUnique";
        public static final String TEMPORAL_EXPRESSION_FREQUENCY_REQUIRED_KEY = "temporalExpression.every.required";
        public static final String TEMPORAL_EXPRESSION_UNKNOWN_UNIT_KEY = "temporalExpression.unknown.unit";
        public static final String TEMPORAL_EXPRESSION_FREQUENCY_MUST_BE_STRICTLY_POSITIVE_KEY = "temporalExpression.every.count.positive";
        public static final String TEMPORAL_EXPRESSION_OFFSET_MUST_BE_POSITIVE_KEY = "temporalExpression.offset.count.positive";
        public static final String COM_TASK_USAGES_NOT_FOUND = "comTaskUsagesNotFound";

        public static final String TOO_LONG = "tooLong";
        public static final String CANNOT_ADD_COM_TASK_TO_COMSCHEDULE_THAT_IS_IN_USE = "cannotAddComTaskToComScheduleInUse";
    }

}
