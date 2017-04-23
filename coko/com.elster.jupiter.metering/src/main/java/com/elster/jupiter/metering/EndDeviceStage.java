/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Models the {@link com.elster.jupiter.fsm.Stage}s
 * that are part of the default enddevice {@link com.elster.jupiter.fsm.StageSet}.
 */
public enum EndDeviceStage {
    PRE_OPERATIONAL("mtr.enddevicestage.preoperational"),
    OPERATIONAL("mtr.enddevicestage.operational"),
    POST_OPERATIONAL("mtr.enddevicestage.postoperational");

    private String key;

    EndDeviceStage(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public static EndDeviceStage fromKey(String key) {
        return Arrays.stream(values())
                .filter(stage -> stage.getKey().equals(key))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid key for end device stage"));
    }

    public static Set<EndDeviceStage> fromKeys(Set<String> keys) {
        return Arrays.stream(values())
                .filter(stage -> keys.contains(stage.getKey()))
                .collect(Collectors.toSet());
    }
}
