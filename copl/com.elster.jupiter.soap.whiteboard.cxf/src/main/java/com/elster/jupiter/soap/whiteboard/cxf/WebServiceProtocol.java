/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf;

/**
 * Created by bvn on 7/4/16.
 */
public enum WebServiceProtocol {
    SOAP("soap"),
    REST("rest");

    private final String path;

    WebServiceProtocol(String path) {
        this.path = path;
    }

    public String path() {
        return path;
    }
}
