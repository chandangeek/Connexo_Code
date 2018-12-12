/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.appserver.rest.impl;

import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {
    APP_SERVER_FAIL_ACTIVATE_TITLE(1, "AppServerActivateTitle", "Failed to activate ''{0}''"),
    APP_SERVER_FAIL_DEACTIVATE_TITLE(2, "AppServerDeActivateTitle", "Failed to deactivate ''{0}''"),
    APP_SERVER_FAIL_ACTIVATE_BODY(3, "AppServerActivateBody", "{0} has changed since the page was last updated."),
    APP_SERVER_FAIL_DEACTIVATE_BODY(4, "AppServerDeActivateBody", "{0} has changed since the page was last updated."),
    INVALIDCHARS_EXCEPTION(5, "InvalidChars", "Characters {0} are not allowed."),
    INVALID_PATH(6, "InvalidPath", "Invalid path")

    ;
    private final int number;
    private final String key;
    private final String format;

    MessageSeeds(int number, String key, String format) {
        this.number = number;
        this.key = key;
        this.format = format;
    }

    @Override
    public String getModule() {
        return AppServerApplication.COMPONENT_NAME;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return format;
    }

    @Override
    public Level getLevel() {
        return Level.SEVERE;
    }
}
