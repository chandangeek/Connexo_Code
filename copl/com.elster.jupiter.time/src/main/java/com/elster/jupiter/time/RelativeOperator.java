package com.elster.jupiter.time;

import java.time.ZonedDateTime;
import java.util.UnknownFormatConversionException;

/**
 * Created by borunova on 01.10.2014.
 */
public enum RelativeOperator {
    PLUS("+") {
        @Override
        public ZonedDateTime apply(ZonedDateTime dateTime, RelativeField field, long value) {
            return dateTime.plus(value, field.getChronoUnit());
        }
    },
    MINUS("-") {
        @Override
        public ZonedDateTime apply(ZonedDateTime dateTime, RelativeField field, long value) {
            return dateTime.minus(value, field.getChronoUnit());
        }
    },
    EQUAL("=") {
        @Override
        public ZonedDateTime apply(ZonedDateTime dateTime, RelativeField field, long value) {
            if (field.getChronoField() == null) {
                throw new UnknownFormatConversionException("Unsupportable operator was used for ChronoField type of field");
            }
            return dateTime.with(field.getChronoField(), value);
        }
    };

    private final String operator;

    private RelativeOperator(String operator) {
        this.operator = operator;
    }

    @Override
    public String toString() {
        return operator;
    }

    public static RelativeOperator from(String operator) {
        switch(operator) {
            case "+":
                return RelativeOperator.PLUS;
            case "-":
                return RelativeOperator.MINUS;
            case "=":
                return RelativeOperator.EQUAL;
            default:
                throw new IllegalArgumentException("Unknown operation");
        }
    }

    public abstract ZonedDateTime apply(ZonedDateTime dateTime, RelativeField field, long value);
}
