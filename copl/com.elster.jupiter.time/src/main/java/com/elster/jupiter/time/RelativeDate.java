package com.elster.jupiter.time;

import com.elster.jupiter.util.UpdatableHolder;

import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RelativeDate {
    private static String SEPARATOR = ";";
    private String relativeDate;
    private List<RelativeOperation> operations = new ArrayList<>();

    public RelativeDate() {
        this.relativeDate = "";
    }

    public RelativeDate(String pattern) {
        relativeDate = pattern;
        setOperations();
    }

    public RelativeDate(List<RelativeOperation> operations) {
        StringBuilder builder = new StringBuilder();
        for(RelativeOperation operation : operations) {
            builder.append(operation.toString()).append(SEPARATOR);
        }
        relativeDate = builder.toString();
        this.operations = operations;
    }

    public RelativeDate(RelativeOperation... operations) {
        this(Arrays.asList(operations));
    }

    public ZonedDateTime getRelativeDate(ZonedDateTime referenceDate) {
        UpdatableHolder<ZonedDateTime> result = new UpdatableHolder<>(referenceDate);

        operationsToApply().forEach(op -> result.update(op::performOperation));

        List<RelativeOperation> lastDayOfMonth = operationsToApply().filter(operation ->
                operation.getField().equals(RelativeField.DAY)
                && operation.getOperator().equals(RelativeOperator.EQUAL)
                && operation.getShift() == RelativeField.LAST_DAY_OF_MONTH).collect(Collectors.toList());
        if(!lastDayOfMonth.isEmpty()) {
            ZonedDateTime date = result.get();
            date = date.with(TemporalAdjusters.lastDayOfMonth());
            result.update(date);
        }
        return result.get();
    }

    private Stream<RelativeOperation> operationsToApply() {
        boolean autoMillisToZero = getOperations().stream().noneMatch(op -> RelativeField.MILLIS.equals(op.getField()));

        Stream<RelativeOperation> operationStream = getOperations().stream();

        if (autoMillisToZero) {
            operationStream = Stream.concat(operationStream, Collections.singleton(RelativeField.MILLIS.equalTo(0L)).stream());
        }
        return operationStream;
    }

    public String getRelativeDate() {
        return this.relativeDate;
    }

    public List<RelativeOperation> getOperations() {
        if(this.operations.isEmpty() && !this.relativeDate.isEmpty()) {
            setOperations();
        }
        return this.operations;
    }

    private void setOperations() {
        String[] operationStrings = relativeDate.split(SEPARATOR);
        for(String operationString : operationStrings) {
            operations.add(getOperation(operationString));
        }
    }

    private RelativeOperation getOperation(String operationString) {
        String[] operationParts = operationString.split(RelativeOperation.SEPARATOR);
        return new RelativeOperation(RelativeField.from(Integer.parseInt(operationParts[0])), RelativeOperator.from(operationParts[1]), Long.parseLong(operationParts[2]));
    }
}
