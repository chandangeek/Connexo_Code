/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cim.webservices.inbound.soap.usagepointconfig;

import com.elster.jupiter.usagepoint.lifecycle.config.DefaultState;

import java.util.Arrays;
import java.util.Optional;

final class DefaultUsagePointStateFinder {
    private DefaultUsagePointStateFinder() {
    }

    static Optional<DefaultState> findForKey(String key) {
        return Arrays.stream(DefaultState.values())
                .filter(state -> state.getKey().equals(key))
                .findFirst();
    }

    static Optional<DefaultState> findForName(String name) {
        return Arrays.stream(DefaultState.values())
                .filter(state -> state.getTranslation().getDefaultFormat().equals(name))
                .findFirst();
    }
}
