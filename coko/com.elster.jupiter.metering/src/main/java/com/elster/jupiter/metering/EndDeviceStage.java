/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Models the {@link com.elster.jupiter.fsm.Stage}s
 * that are part of the default {@link com.elster.jupiter.fsm.StageSet}.
 */
public enum EndDeviceStage {
    PRE_OPERATIONAL,
    OPERATIONAL,
    POST_OPERATIONAL;

    public static Set<EndDeviceStage> fromNames(Set<String> names) {
        return Arrays.stream(values())
                .filter(stage -> names.contains(stage.name()))
                .collect(Collectors.toSet());
    }
}
