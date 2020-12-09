package com.elster.jupiter.soap.whiteboard.cxf;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

public enum WebServiceCallOccurrenceStatus implements TranslationKey {
    ONGOING("Ongoing"),
    FAILED("Failed"),
    SUCCESSFUL("Successful"),
    CANCELLED("Cancelled");

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

    @Override
    public String getKey() {
        return "webservices.occurrence.status." + name().toLowerCase();
    }

    @Override
    public String getDefaultFormat() {
        return name;
    }

    public String translate(Thesaurus thesaurus) {
        return thesaurus.getFormat(this).format();
    }

    public static WebServiceCallOccurrenceStatus fromString(String text) {
        for (WebServiceCallOccurrenceStatus status : WebServiceCallOccurrenceStatus.values()) {
            if (status.name().equalsIgnoreCase(text)) {
                return status;
            }
        }
        throw new IllegalArgumentException();
    }
}
