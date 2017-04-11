/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.config;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Models the {@link com.elster.jupiter.fsm.Stage}s
 * that are part of the default usagepoint {@link com.elster.jupiter.fsm.StageSet}.
 */
public enum UsagePointStage {
    PRE_OPERATIONAL("mtr.usagepointstage.preoperational"),
    OPERATIONAL("mtr.usagepointstage.operational"),
    POST_OPERATIONAL("mtr.usagepointstage.postoperational"),
    SUSPENDED("mtr.usagepointstage.suspended")

    private String key;

    UsagePointStage(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public static Set<UsagePointStage> fromKeys(Set<String> keys) {
        return Arrays.stream(values())
                .filter(stage -> keys.contains(stage.getKey()))
                .collect(Collectors.toSet());
    }
}