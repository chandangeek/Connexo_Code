/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.messaging.rest.impl;

import com.elster.jupiter.messaging.DestinationSpec;

import java.util.Arrays;
import java.util.function.Predicate;

public enum DestinationType {
    QUEUE(DestinationSpec::isQueue),
    TOPIC(DestinationSpec::isTopic);

    private final Predicate<DestinationSpec> matcher;

    DestinationType(Predicate<DestinationSpec> matcher) {
        this.matcher = matcher;
    }

    private boolean matches(DestinationSpec destinationSpec) {
        return matcher.test(destinationSpec);
    }

    public static DestinationType typeOf(DestinationSpec destinationSpec) {
        return Arrays.stream(values())
                .filter(type -> type.matches(destinationSpec))
                .findAny()
                .orElseThrow(IllegalStateException::new);
    }
}
