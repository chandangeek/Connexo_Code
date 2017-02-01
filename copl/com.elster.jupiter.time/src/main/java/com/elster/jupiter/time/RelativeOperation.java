/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.time;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by borunova on 01.10.2014.
 */
public final class RelativeOperation implements Comparable<RelativeOperation>{
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
        return operator.apply(referenceDate, field, shift);
    }

    public static List<RelativeOperation> from(ZonedDateTime fixedDateTime) {
        List<RelativeOperation> relativeOperations = new ArrayList<>();
        relativeOperations.add(new RelativeOperation(RelativeField.YEAR, RelativeOperator.EQUAL, fixedDateTime.getYear()));
        relativeOperations.add(new RelativeOperation(RelativeField.MONTH, RelativeOperator.EQUAL, fixedDateTime.getMonthValue()));
        relativeOperations.add(new RelativeOperation(RelativeField.DAY, RelativeOperator.EQUAL, fixedDateTime.getDayOfMonth()));
        relativeOperations.add(new RelativeOperation(RelativeField.HOUR, RelativeOperator.EQUAL, fixedDateTime.getHour()));
        relativeOperations.add(new RelativeOperation(RelativeField.MINUTES, RelativeOperator.EQUAL, fixedDateTime.getMinute()));

        return relativeOperations;
    }

    @Override
    public String toString() {
        return String.valueOf(field.getId()) + SEPARATOR + operator.toString() + SEPARATOR + shift;
    }

    @Override
    public int compareTo(RelativeOperation o) {
        return this.getField().getPriority().compareTo(o.getField().getPriority());
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RelativeOperation that = (RelativeOperation) o;

        return shift == that.shift && field == that.field && operator == that.operator;

    }

    @Override
    public int hashCode() {
        return Objects.hash(shift, field, operator);
    }

    private void validateOperationOrThrowException(RelativeField field, RelativeOperator operator, long shift) {
        if (!field.isValid(shift, operator)) {
            throw new IllegalArgumentException("Provided value is incorrect");
        }
    }

}