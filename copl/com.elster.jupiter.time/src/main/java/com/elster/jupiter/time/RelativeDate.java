package com.elster.jupiter.time;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by borunova on 01.10.2014.
 */
public class RelativeDate {
    private static String SEPARATOR = ";";
    private String relativeDate;
    private List<RelativeOperation> operations = new ArrayList<>();;

    public RelativeDate() {};

    public RelativeDate(String pattern) {
        relativeDate = pattern;
        String[] operationStrings = relativeDate.split(SEPARATOR);
        for(String operationString : operationStrings) {
            operations.add(getOperation(operationString));
        }
    }

    public RelativeDate(List<RelativeOperation> operations) {
        StringBuilder builder = new StringBuilder();
        for(RelativeOperation operation : operations) {
            builder.append(operation.toString()).append(SEPARATOR);
        }
        relativeDate = builder.toString();
        this.operations = operations;
    }

    public ZonedDateTime getRelativeDate(ZonedDateTime referenceDate) {
        ZonedDateTime relativeDate = referenceDate;

        for(RelativeOperation operation : getOperations()) {
            relativeDate = operation.performOperation(relativeDate);
        }
        return relativeDate;
    }

    public String getRelativeDate() {
        return this.relativeDate;
    }

    public List<RelativeOperation> getOperations() {
        return this.operations;
    }

    private RelativeOperation getOperation(String operationString) {
        String[] operationParts = operationString.split(RelativeOperation.SEPARATOR);
        return new RelativeOperation(RelativeField.from(Integer.parseInt(operationParts[0])), RelativeOperator.from(operationParts[1]), Long.parseLong(operationParts[2]));
    }
}
