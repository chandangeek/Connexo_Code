/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.math.BigDecimal;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public enum BigDecimalFunction {

    MULTIPLY("MULTIPLY", BigDecimal::multiply),
    ADD("ADD", BigDecimal::add),
    SUBTRACT("SUBTRACT", BigDecimal::subtract);

    private final String jsonName;
    private final BiFunction<BigDecimal, BigDecimal, BigDecimal> function;

    BigDecimalFunction(String jsonName, BiFunction<BigDecimal, BigDecimal, BigDecimal> function) {
        this.jsonName = jsonName;
        this.function = function;
    }

    @JsonCreator
    public static BigDecimalFunction forValue(String jsonValue) {
        return Stream.of(values())
                .filter(level -> level.jsonName.equals(jsonValue))
                .findAny()
                .orElse(null);
    }

    @JsonValue
    public String toValue() {
        return jsonName;
    }

    public BigDecimal apply(BigDecimal value, BigDecimal correction) {
        return function.apply(value, correction);
    }
}