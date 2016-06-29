package com.elster.jupiter.time;

import com.elster.jupiter.util.UpdatableHolder;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class RelativeDate {
    public static final String NOW_STRING = "now";
    private static String SEPARATOR = ";";
    private String relativeDate;
    private List<RelativeOperation> operations = new ArrayList<>();

    public static RelativeDate NOW = new RelativeDate();

    public RelativeDate() {
        this.relativeDate = NOW_STRING;
    }

    public RelativeDate(String pattern) {
        relativeDate = pattern;
        setOperations();
    }

    public RelativeDate(List<RelativeOperation> operations) {
        StringBuilder builder = new StringBuilder();
        for (RelativeOperation operation : operations) {
            builder.append(operation.toString()).append(SEPARATOR);
        }
        relativeDate = builder.toString();
        if (relativeDate.isEmpty()) {
            relativeDate = NOW_STRING;
        }
        this.operations.addAll(operations);
    }

    public RelativeDate(RelativeOperation... operations) {
        this(Arrays.asList(operations));
    }

    public RelativeDate of(RelativeOperation... operations) {
        if (operations.length == 0) {
            return NOW;
        }
        return new RelativeDate(operations);
    }

    public ZonedDateTime getRelativeDate(ZonedDateTime referenceDate) {
        UpdatableHolder<ZonedDateTime> result = new UpdatableHolder<>(referenceDate);

        operationsToApply().forEach(op -> result.update(op::performOperation));
        return result.get();
    }

    public RelativeDate with(RelativeOperation... operations) {
        List<RelativeOperation> toAdd = new ArrayList<>(Arrays.asList(operations));
        List<RelativeOperation> toBuild = this.operations.stream()
                .map(op -> toAdd.stream().filter(add -> add.getField().equals(op.getField())).findFirst().orElse(op))
                .collect(Collectors.toCollection(ArrayList::new));
        toAdd.removeIf(add -> toBuild.stream().map(RelativeOperation::getField).anyMatch(field -> field.equals(add.getField())));
        toBuild.addAll(toAdd);
        return new RelativeDate(toBuild);
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
        if (this.operations.isEmpty() && !this.relativeDate.isEmpty()) {
            setOperations();
        }
        Collections.sort(this.operations);
        return this.operations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RelativeDate that = (RelativeDate) o;

        return operations.equals(that.operations);

    }

    @Override
    public int hashCode() {
        return operations.hashCode();
    }

    private void setOperations() {
        if (NOW_STRING.equals(relativeDate)) {
            return;
        }
        String[] operationStrings = relativeDate.split(SEPARATOR);
        for (String operationString : operationStrings) {
            operations.add(getOperation(operationString));
        }
    }

    private RelativeOperation getOperation(String operationString) {
        String[] operationParts = operationString.split(RelativeOperation.SEPARATOR);
        return new RelativeOperation(RelativeField.from(Integer.parseInt(operationParts[0])), RelativeOperator.from(operationParts[1]), Long.parseLong(operationParts[2]));
    }

    public static Collector<RelativeOperation, ArrayList<RelativeOperation>, RelativeDate> collect() {
        return new Collector<RelativeOperation, ArrayList<RelativeOperation>, RelativeDate>() {
            @Override
            public Supplier<ArrayList<RelativeOperation>> supplier() {
                return ArrayList::new;
            }

            @Override
            public BiConsumer<ArrayList<RelativeOperation>, RelativeOperation> accumulator() {
                return (list, op) -> list.add(op);
            }

            @Override
            public BinaryOperator<ArrayList<RelativeOperation>> combiner() {
                return (a, b) -> {
                    a.addAll(b);
                    return a;
                };
            }

            @Override
            public Function<ArrayList<RelativeOperation>, RelativeDate> finisher() {
                return RelativeDate::new;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Collections.emptySet();
            }
        };
    }
}
