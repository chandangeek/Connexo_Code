package com.elster.jupiter.soap.whiteboard.cxf;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public enum WebServiceCallOccurrenceStatus {
    ONGOING("Ongoing"),
    FAILED("Failed"),
    SUCCESSFUL("Successful");

    private String name;

    public String getName() {
        return name;
    }

    WebServiceCallOccurrenceStatus(String name) {
        this.name = name;
    }
}
