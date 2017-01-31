/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservices.rest.impl;

import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {
    NO_SUCH_END_POINT_CONFIG(1, "NoSuchEndPointConfig", "No such end point configurations exists"),
    PAYLOAD_EXPECTED(2, "PayloadExpected", "This method requires a payload"),
    FIELD_EXPECTED(3, "FieldIsRequired", "This field is required"),
    NO_SUCH_WEB_SERVICE(4, "NoSuchWebService", "No such web service was registered"),
    NO_SUCH_GROUP(5, "NoSuchGroup", "No such role");

    private final int number;
    private final String key;
    private final String defaultFormat;

    MessageSeeds(int number, String key, String defaultFormat) {
        this.number = number;
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getModule() {
        return WebServicesApplication.COMPONENT_NAME;
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
        return defaultFormat;
    }

    @Override
    public Level getLevel() {
        return Level.SEVERE;
    }

}
