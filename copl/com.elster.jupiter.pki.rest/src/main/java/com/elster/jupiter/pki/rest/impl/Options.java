package com.elster.jupiter.pki.rest.impl;

import com.elster.jupiter.rest.util.IdWithNameInfo;

import java.util.List;

public class Options {

    private List<IdWithNameInfo> options;

    // needed by marshaller
    public Options(){
    }


    public Options(List<IdWithNameInfo> options) {
        this.options = options;
    }

    public List<IdWithNameInfo> getOptions() {
        return options;
    }

    public void setOptions(List<IdWithNameInfo> options) {
        this.options = options;
    }
}
