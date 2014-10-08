package com.elster.jupiter.time;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.UnknownFormatConversionException;

/**
 * Created by borunova on 01.10.2014.
 */
public class RelativeOperation {
    public static String SEPARATOR = ":";

    private RelativeField field;
    private RelativeOperator operator;
    private long shift;

    public RelativeOperation() {
    }

    public RelativeOperation(RelativeField field, RelativeOperator operator, long shift) {
        validateOperationOrThrowException(field, operator, shift);
        this.field = field;
        this.operator = operator;
        this.shift = shift;
    }

    public ZonedDateTime performOperation(ZonedDateTime referenceDate) {
        ZonedDateTime relativeDate = referenceDate;
        if (field.isChronoUnitBased()) {
            switch (operator) {
                case PLUS:
                    relativeDate = referenceDate.plus(shift, field.getChronoUnit());
                    break;
                case MINUS:
                    relativeDate = referenceDate.minus(shift, field.getChronoUnit());
                    break;
                default:
                    throw new UnknownFormatConversionException("Unsupportable operator was used for ChronoUnit type of field");
            }
        } else if (field.isChronoFieldBased()) {
            switch (operator) {
                case EQUAL:
                    relativeDate = referenceDate.with(field.getChronoField(), shift);
                    break;
                default:
                    throw new UnknownFormatConversionException("Unsupportable operator was used for ChronoField type of field");
            }

        } else if (field.equals(RelativeField.CURRENT_DAY_OF_MONTH)) {
                ZonedDateTime now = ZonedDateTime.ofInstant(Instant.now(), referenceDate.getZone());
                relativeDate = referenceDate.withDayOfMonth(now.getDayOfMonth());
        } else if (field.equals(RelativeField.START_NOW)) {
            ZonedDateTime now = ZonedDateTime.ofInstant(Instant.now(), referenceDate.getZone());
            relativeDate = referenceDate.withDayOfMonth(now.getDayOfMonth()).withMonth(now.getMonth().getValue()).withYear(now.getYear());
        }
        return relativeDate;
    }

    public static List<RelativeOperation> from(ZonedDateTime fixedDateTime) {
        List<RelativeOperation> relativeOperations = new ArrayList<>();
        relativeOperations.add(new RelativeOperation(RelativeField.YEAR, RelativeOperator.EQUAL, fixedDateTime.getYear()));
        relativeOperations.add(new RelativeOperation(RelativeField.MONTH_IN_YEAR, RelativeOperator.EQUAL, fixedDateTime.getMonthValue()));
        relativeOperations.add(new RelativeOperation(RelativeField.DAY_IN_MONTH, RelativeOperator.EQUAL, fixedDateTime.getDayOfMonth()));
        relativeOperations.add(new RelativeOperation(RelativeField.HOUR_OF_DAY, RelativeOperator.EQUAL, fixedDateTime.getHour()));
        relativeOperations.add(new RelativeOperation(RelativeField.MINUTES_OF_HOUR, RelativeOperator.EQUAL, fixedDateTime.getMinute()));

        return relativeOperations;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(field.getId()).append(SEPARATOR)
                .append(operator.toString()).append(SEPARATOR)
                .append(shift);
        return builder.toString();
    }

    public long getShift() {
        return shift;
    }

    public void setShift(long shift) {
        this.shift = shift;
    }

    public RelativeOperator getOperator() {
        return operator;
    }

    public void setOperator(RelativeOperator operator) {
        this.operator = operator;
    }

    public RelativeField getField() {
        return field;
    }

    public void setField(RelativeField field) {
        this.field = field;
    }

    private void validateOperationOrThrowException(RelativeField field, RelativeOperator operator, long shift) {
        if (!field.isValid(shift)) {
            throw new IllegalArgumentException("Provided value is incorrect");
        }
        if (field.isChronoUnitBased() && !(operator.equals(RelativeOperator.PLUS) || operator.equals(RelativeOperator.MINUS))) {
            StringBuilder exception = new StringBuilder();
            exception.append("Unsupportable operator \"").append(operator.toString())
                    .append("\" was used for ChronoUnit type of field. Valid operators are \"")
                    .append(RelativeOperator.PLUS.toString()).append("\" and \"")
                    .append(RelativeOperator.MINUS.toString()).append("\"");
            throw new UnknownFormatConversionException(exception.toString());
        }
        if (field.isChronoFieldBased() && !operator.equals(RelativeOperator.EQUAL)) {
            StringBuilder exception = new StringBuilder();
            exception.append("Unsupportable operator \"").append(operator.toString())
                    .append("\" was used for ChronoUnit type of field. Valid operators are \"")
                    .append(RelativeOperator.EQUAL.toString()).append("\"");
            throw new UnknownFormatConversionException(exception.toString());
        }
    }
}
