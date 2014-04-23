package com.energyict.mdc.scheduling.model.impl;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.scheduling.SchedulingService;
import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {
    SHOULD_BE_AT_LEAST(1, Constants.VALUE_TOO_SMALL, "Minimal acceptable value is {min}, was {value}", Level.SEVERE),
    CAN_NOT_BE_EMPTY(2, Constants.CAN_NOT_BE_EMPTY, "This field can not be empty", Level.SEVERE),
    VALUE_NOT_IN_RANGE(3, Constants.VALUE_NOT_IN_RANGE, "{value} not in range {min} to {max}", Level.SEVERE),
    INVALID_URL(4, Constants.INVALID_URL, "{value} is not a valid URL", Level.SEVERE),
    INVALID_CHARS(5, Constants.INVALID_CHARS, "This field contains invalid chars, should obey {regex}", Level.SEVERE),
    REQUIRED_FOR_HTTPS(6, Constants.CAN_NOT_BE_EMPTY_IF_HTTPS, "This field is mandatory in case https is chosen", Level.SEVERE),
    NOT_UNIQUE(7, Constants.NOT_UNIQUE, "field should be unique", Level.SEVERE),
    NEXT_EXECUTION_SPEC_OFFSET_IS_GREATER_THAN_FREQUENCY(8, Constants.NEXT_EXECUTION_SPEC_OFFSET_IS_GREATER_THAN_FREQUENCY_KEY, "Next Execution Spec's offset is greater than its frequency.", Level.SEVERE),
    NEXT_EXECUTION_SPECS_TEMPORAL_EXPRESSION_REQUIRED(9, Constants.NEXT_EXECUTION_SPECS_TEMPORAL_EXPRESSION_REQUIRED_KEY, "The temporal expression of a NextExecutionSpec is required", Level.SEVERE),
    TEMPORAL_EXPRESSION_FREQUENCY_REQUIRED(10, Constants.TEMPORAL_EXPRESSION_FREQUENCY_REQUIRED_KEY, "The frequency of a temporal expression is required", Level.SEVERE),
    TEMPORAL_EXPRESSION_UNKNOWN_UNIT(11, Constants.TEMPORAL_EXPRESSION_UNKNOWN_UNIT_KEY, "The unit {0} is unknown or unsupported for temporal expressions", Level.SEVERE),
    TEMPORAL_EXPRESSION_FREQUENCY_MUST_BE_STRICTLY_POSITIVE(12, Constants.TEMPORAL_EXPRESSION_FREQUENCY_MUST_BE_STRICTLY_POSITIVE_KEY, "The frequency value of a temporal expression must be a strictly positive number", Level.SEVERE),
    TEMPORAL_EXPRESSION_OFFSET_MUST_BE_POSITIVE(13, Constants.TEMPORAL_EXPRESSION_OFFSET_MUST_BE_POSITIVE_KEY, "The offset value of a temporal expression must be a positive number", Level.SEVERE);

    private final int number;
    private final String key;
    private final String defaultFormat;
    private final Level level;

    MessageSeeds(int number, String key, String defaultFormat, Level level) {
        this.number = number;
        this.key = stripComponentNameIfPresent(key);
        this.defaultFormat = defaultFormat;
        this.level = level;
    }

    private String stripComponentNameIfPresent(String key) {
        if (key.startsWith(SchedulingService.COMPONENT_NAME+".")) {
            return key.substring(SchedulingService.COMPONENT_NAME.length()+1);
        } else {
            return key;
        }
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

}

final class Constants {

    public static final String VALUE_TOO_SMALL = SchedulingService.COMPONENT_NAME+".ValueTooSmall";
    public static final String CAN_NOT_BE_EMPTY = SchedulingService.COMPONENT_NAME+".CanNotBeEmpty";
    public static final String VALUE_NOT_IN_RANGE = SchedulingService.COMPONENT_NAME+".ValueNotInRange";
    public static final String INVALID_URL = SchedulingService.COMPONENT_NAME+".InvalidURL";
    public static final String INVALID_CHARS = SchedulingService.COMPONENT_NAME+".InvalidChars";
    public static final String CAN_NOT_BE_EMPTY_IF_HTTPS = SchedulingService.COMPONENT_NAME+".CanNotBeEmptyIfHttps";
    public static final String NEXT_EXECUTION_SPEC_OFFSET_IS_GREATER_THAN_FREQUENCY_KEY = SchedulingService.COMPONENT_NAME+".nextExecutionSpecs.offsetGreaterThanFrequency";
    public static final String NEXT_EXECUTION_SPECS_TEMPORAL_EXPRESSION_REQUIRED_KEY = SchedulingService.COMPONENT_NAME+".nextExecutionSpecs.temporalExpression.required";
    public static final String NOT_UNIQUE = SchedulingService.COMPONENT_NAME+".notUnique";
    public static final String TEMPORAL_EXPRESSION_FREQUENCY_REQUIRED_KEY = SchedulingService.COMPONENT_NAME+".temporalExpression.every.required";
    public static final String TEMPORAL_EXPRESSION_UNKNOWN_UNIT_KEY = SchedulingService.COMPONENT_NAME+".temporalExpression.unknown.unit";
    public static final String TEMPORAL_EXPRESSION_FREQUENCY_MUST_BE_STRICTLY_POSITIVE_KEY = SchedulingService.COMPONENT_NAME+".temporalExpression.every.count.positive";
    public static final String TEMPORAL_EXPRESSION_OFFSET_MUST_BE_POSITIVE_KEY = SchedulingService.COMPONENT_NAME+".temporalExpression.offset.count.positive";

}


