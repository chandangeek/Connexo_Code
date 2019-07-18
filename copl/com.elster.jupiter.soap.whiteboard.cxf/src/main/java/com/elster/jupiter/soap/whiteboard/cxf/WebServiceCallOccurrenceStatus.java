package com.elster.jupiter.soap.whiteboard.cxf;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

public enum WebServiceCallOccurrenceStatus implements TranslationKey {
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

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String getKey() {
        return "com.elster.jupiter.soap.whiteboard.cxf." + name();
    }

    @Override
    public String getDefaultFormat() {
        return name;
    }

    public String getDisplayName(Thesaurus thesaurus) {
        return thesaurus.getFormat(this).format();
    }

}
