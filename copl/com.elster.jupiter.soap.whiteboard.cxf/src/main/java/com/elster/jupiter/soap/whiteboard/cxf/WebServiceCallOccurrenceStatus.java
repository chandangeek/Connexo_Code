package com.elster.jupiter.soap.whiteboard.cxf;

public enum WebServiceCallOccurrenceStatus {
    ONGOING("ongoing"),
    FAILED("failed"),
    SUCCESSFUL("successful");

    private String name;

    public String getName() {
        return name;
    }

    WebServiceCallOccurrenceStatus(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public static WebServiceCallOccurrenceStatus fromString(String text) {
        for (WebServiceCallOccurrenceStatus status : WebServiceCallOccurrenceStatus.values()) {
            if (status.name.equalsIgnoreCase(text)) {
                return status;
            }
        }
        throw new IllegalArgumentException();
    }
}
