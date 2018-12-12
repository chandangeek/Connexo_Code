/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.http.whiteboard.impl;

public enum WhiteBoardProperties {

    SYSTEM_IDENTIFIER("com.elster.jupiter.system.identifier", ""),
    SYSTEM_IDENTIFIER_COLOR("com.elster.jupiter.system.identifier.color", "#FFFFFF"),

    ;

    private String key;
    private String defaultValue;

    WhiteBoardProperties(String key, String defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    public String getKey() {
        return key;
    }

    public String getDefaultValue() {
        return defaultValue;
    }
}