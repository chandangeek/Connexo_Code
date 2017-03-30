/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.config;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Models the supported mathematical operators that can be used in {@link com.elster.jupiter.metering.config.Formula}'s
 * of {@link com.elster.jupiter.metering.config.ReadingTypeDeliverable}s.
 *
 * @author Isabelle Gheysens (igh)
 * @since 2016-02-04
 */
public enum Operator {
    PLUS,
    MINUS,
    MULTIPLY,
    DIVIDE,
    SAFE_DIVIDE;

    public String toString() {
        return this.name().toLowerCase();
    }

    public static Set<String> names() {
        return Stream.of(values()).map(Operator::toString).collect(Collectors.toSet());
    }

    public static Optional<Operator> from(String name) {
        return Stream.of(values()).filter(each -> each.toString().equals(name)).findFirst();
    }

}