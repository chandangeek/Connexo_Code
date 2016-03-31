package com.elster.jupiter.metering.config;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Models the supported functions that can be used in {@link com.elster.jupiter.metering.config.Formula}'s
 * of {@link com.elster.jupiter.metering.config.ReadingTypeDeliverable}s.
 *
 * @author Isabelle Gheysens (igh)
 * @since 2016-02-08
 */
public enum Function {
    SUM,
    MAX,
    MIN,
    AVG,
    AGG_TIME {
        @Override
        public String toString() {
            return "agg";
        }
    },
    FIRST_NOT_NULL {
        @Override
        public String toString() {
            return "firstNotNull";
        }
    };

    public String toString() {
        return this.name().toLowerCase();
    }

    public static Set<String> names() {
        return Stream.of(values()).map(Function::toString).collect(Collectors.toSet());
    }

    public static Optional<Function> from(String name) {
        return Stream.of(values()).filter(each -> each.toString().equals(name)).findFirst();
    }

}