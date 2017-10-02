/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.stream.Stream;

public class Expiration {

    public enum Type {
        EXPIRED("expired"),
        EXPIRES_1WEEK("expires_1week"),
        EXPIRES_1MONTH("expires_1month"),
        EXPIRES_3MONTHS("expires_3months");

        private final String name;

        Type(String name) {
            this.name = name;
        }

        @JsonValue
        String getName() {
            return name;
        }

        @JsonCreator
        public static Expiration.Type fromString(String name) {
            return Stream.of(values())
                    .filter(type -> type.name.equals(name))
                    .findAny()
                    .orElse(null);
        }
    }

    private Type type;

    public Expiration(Type type) {
        this.type = type;
    }

    @JsonProperty
    public Type getType() {
        return type;
    }
}
