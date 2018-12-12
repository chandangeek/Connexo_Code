/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import java.math.BigDecimal;
import java.util.stream.Stream;

public class TwoValuesDifference {

    public enum Type {
        ABSOLUTE("absolute"),
        RELATIVE("relative");

        private final String name;

        Type(String name) {
            this.name = name;
        }

        @JsonValue
        String getName() {
            return name;
        }

        @JsonCreator
        public static Type fromString(String name) {
            return Stream.of(values())
                    .filter(type -> type.name.equals(name))
                    .findAny()
                    .orElse(null);
        }
    }

    private Type type;
    private BigDecimal value;

    public TwoValuesDifference(Type type, BigDecimal value) {
        this.type = type;
        this.value = value;
    }

    @JsonProperty
    public Type getType() {
        return type;
    }

    @JsonProperty
    public BigDecimal getValue() {
        return value;
    }
}
