/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

/**
 * Created by bvn on 5/3/16.
 */
public enum MessageSeeds implements MessageSeed {

    FIELD_REQUIRED(1, Keys.FIELD_REQUIRED, "This field is required"),
    FIELD_TOOL_LONG(2, Keys.FIELD_TOO_LONG, "This field is too long"),
    FIELD_NOT_UNIQUE(3, Keys.FIELD_MUST_BE_UNIQUE, "This field must be unique"),
    INVALID_FILE_NAME(4, Keys.INVALID_FILE_NAME, "Invalid file name"),
    INVALID_PATH(5, Keys.INVALID_PATH, "Invalid path"),
    FIELD_IS_TOO_LONG(6, Keys.FIELD_IS_TOO_LONG, "''{0}'' value is too long."),

    NO_WEB_SERVICE_ENDPOINT(100, "NoWebServiceEndpoint", "Can''t send request to web service endpoint ''{0}'': it isn''t available."),
    INACTIVE_WEB_SERVICE_ENDPOINT(101, "InactiveWebServiceEndpoint", "Can''t send request to web service endpoint ''{0}'': it is deactivated."),
    NO_WEB_SERVICE_ENDPOINTS(102, "NoWebServiceEndpoints", "No web service endpoints are available to send the request using ''{0}''."),
    PUBLISHING_WEB_SERVICE_ENDPOINT(103, "PublishingWebServiceEndpoint", "Web service endpoint ''{0}'' is active but unpublished; publishing now...", Level.CONFIG),
    WRONG_WEB_SERVICE_ENDPOINT_CONFIGURATION(104, "WrongWebServiceEndpointConfiguration", "Can''t send request to web service endpoint ''{0}'' with the help of web service ''{1}''."),
    WEB_SERVICE_CALL_OCCURRENCE_IS_ALREADY_IN_STATE(110, "WebServiceOccurrenceIsAlreadyInState", "Web service call occurrence is already in ''{0}'' state.");

    private final int number;
    private final String key;
    private final String defaultFormat;
    private final Level level;

    MessageSeeds(int number, String key, String defaultFormat) {
        this(number, key, defaultFormat, Level.SEVERE);
    }

    MessageSeeds(int number, String key, String defaultFormat, Level level) {
        this.number = number;
        this.key = key;
        this.defaultFormat = defaultFormat;
        this.level = level;
    }

    @Override
    public String getModule() {
        return WebServicesService.COMPONENT_NAME;
    }

    @Override
    public int getNumber() {
        return this.number;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

    @Override
    public Level getLevel() {
        return this.level;
    }

    public static interface Keys {
        String FIELD_REQUIRED = "field.required";
        String FIELD_TOO_LONG = "ThisFieldIsTooLong";
        String FIELD_IS_TOO_LONG = "FieldIsTooLong";
        String FIELD_MUST_BE_UNIQUE = "FieldMustBeUnique";
        String INVALID_FILE_NAME = "InvalidFileName";
        String INVALID_PATH = "InvalidPath";
    }

}
