/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.configproperties;

public enum ConfigProperties {
    TRUE_MINIMIZED("communication.settings.true.minimized"),
    RANDOMIZATION("communication.settings.randomization");

    private String value;
    ConfigProperties(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
