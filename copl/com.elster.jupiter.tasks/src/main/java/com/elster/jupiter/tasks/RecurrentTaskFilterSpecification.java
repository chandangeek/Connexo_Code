/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.tasks;

import com.elster.jupiter.util.conditions.Order;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Created by igh on 6/11/2015.
 */
public class RecurrentTaskFilterSpecification {

    public Set<String> applications = new HashSet<>();
    public Set<String> queues = new HashSet<>();
    public Set<String> queueTypes = new HashSet<>();
    public Instant startedOnFrom;
    public Instant startedOnTo;
    public Set<String> suspended = new HashSet<>();
    public Instant nextExecutionFrom;
    public Instant nextExecutionTo;
    public NumberBetweenFilter priority = new NumberBetweenFilter();
    public List<Order> sortingColumns;

    public class NumberBetweenFilter {
        public Operator operator;
        public Long lowerBound;
        public Long upperBound;
    }

    public enum Operator {
        EQUAL("==") {
            @Override
            public void apply(JsonNode criteriaNode, NumberBetweenFilter field) {
                if (criteriaNode.isNumber()) {
                    long value = criteriaNode.numberValue().longValue();
                    field.operator = this;
                    field.lowerBound = field.upperBound = value;
                }
            }
        },
        BETWEEN("BETWEEN") {
            @Override
            public void apply(JsonNode criteriaNode, NumberBetweenFilter field) {
                if (criteriaNode.isArray() && criteriaNode.size() == 2 && criteriaNode.get(0).isNumber() && criteriaNode.get(1).isNumber()) {
                    long lowerBound = criteriaNode.get(0).numberValue().longValue();
                    long upperBound = criteriaNode.get(1).numberValue().longValue();
                    if (lowerBound <= upperBound) {
                        field.operator = this;
                        field.lowerBound = lowerBound;
                        field.upperBound = upperBound;
                    }
                }
            }
        },
        GREATER_THAN(">") {
            @Override
            public void apply(JsonNode criteriaNode, NumberBetweenFilter field) {
                if (criteriaNode.isNumber()) {
                    long value = criteriaNode.numberValue().longValue();
                    field.operator = this;
                    field.lowerBound = value;
                }
            }
        },
        LESS_THAN("<") {
            @Override
            public void apply(JsonNode criteriaNode, NumberBetweenFilter field) {
                if (criteriaNode.isNumber()) {
                    long value = criteriaNode.numberValue().longValue();
                    field.operator = this;
                    field.upperBound = value;
                }
            }
        };

        private String jsonName;

        Operator(String jsonName) {
            this.jsonName = jsonName;
        }

        String jsonName() {
            return this.jsonName;
        }

        abstract public void apply(JsonNode criteriaNode, NumberBetweenFilter field);

        static public Optional<Operator> findOperator(String name) {
            return Stream.of(values()).filter(operator -> operator.jsonName().equals(name)).findAny();
        }
    }
}
