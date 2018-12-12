/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf;

public enum EventType {

    ENDPOINT_CONFIGURATION_CHANGED("endpoint/CHANGED"),
    WEBSERVICE_REGISTERED("endpoint/REGISTERED"),;

    private static final String NAMESPACE = "com/elster/jupiter/webservices/";
    private final String topic;

    EventType(String topic) {
        this.topic = topic;
    }

    public String topic() {
        return NAMESPACE + topic;
    }

}