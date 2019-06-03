package com.elster.jupiter.soap.whiteboard.cxf;

public enum WebServiceCallOccurrenceStatus {

    ONGOING("Ongoing"),
    FAILED("Failed"),
    SUCCESSFUL("Sucessful");

    private String name;

    public String getName(){
        return name;
    }

    WebServiceCallOccurrenceStatus(String name) {
        this.name = name;
    }
}
