/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.http.whiteboard.impl;

public enum WhiteboardEvent {

    LOGIN("LOGIN"),
    LOGOUT("LOGOUT")
    ;
    private static final String NAMESPACE = "com/elster/jupiter/http/whiteboard/";
    private final String topic;

    WhiteboardEvent(String topic) {
        this.topic = topic;
    }

    public String topic() {
        return NAMESPACE + topic;
    }


}