/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.http.whiteboard.impl;

public enum WhiteboardEvent {

    LOGIN("LOGIN"),
    LOGOUT("LOGOUT"),
    LOGIN_FAILED("LOGIN_FAILED"),
    TOKEN_RENEWAL("TOKEN_RENEWAL"),
    TOKEN_EXPIRED("TOKEN_EXPIRED");

    private static final String NAMESPACE = "com/elster/jupiter/http/whiteboard/";
    private final String topic;

    WhiteboardEvent(String topic) {
        this.topic = topic;
    }

    public String topic() {
        return NAMESPACE + topic;
    }

}