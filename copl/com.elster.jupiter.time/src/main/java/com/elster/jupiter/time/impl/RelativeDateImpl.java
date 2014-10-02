package com.elster.jupiter.time.impl;

import com.elster.jupiter.time.RelativeDate;
import com.elster.jupiter.time.RelativeField;
import com.elster.jupiter.time.RelativeOperation;
import com.elster.jupiter.time.RelativeOperator;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by borunova on 01.10.2014.
 */
public class RelativeDateImpl implements RelativeDate {

    private String relativeDate;

    public RelativeDateImpl() {};

    public RelativeDateImpl(String pattern) {
        relativeDate = pattern;
    }

    @Override
    public ZonedDateTime getRelativeDate(ZonedDateTime referenceDate) {
        ZonedDateTime relativeDate = referenceDate;

        for(RelativeOperation operation : getOperations()) {
            relativeDate = operation.performOperation(relativeDate);
        }

        return relativeDate;
    }

    @Override
    public void setPattern(List<RelativeOperation> operationList) {
        StringBuilder builder = new StringBuilder();
        for(RelativeOperation operation : operationList) {
            builder.append(operation.toString()).append(SEPARATOR);
        }
        relativeDate = builder.toString();
    }

    List<RelativeOperation> getOperations() {
        List<RelativeOperation> operations = new ArrayList<>();
        String[] operationStrings = relativeDate.split(SEPARATOR);
        for(String operationString : operationStrings) {
            operations.add(getOperation(operationString));
        }
        return operations;
    }

    RelativeOperation getOperation(String operationString) {
        String[] operationParts = operationString.split(RelativeOperation.SEPARATOR);
        return new RelativeOperation(RelativeField.from(Integer.parseInt(operationParts[0])), RelativeOperator.from(operationParts[1]), Long.parseLong(operationParts[2]));
    }

    public String getRelativeDate() {
        return this.relativeDate;
    }
}
