package com.elster.jupiter.mdm.usagepoint.config.rest;

public class OutputMatchesInfo {

    public String outputName;
    public boolean isMatched;

    public OutputMatchesInfo(String outputName, boolean isMatched) {
        this.outputName = outputName;
        this.isMatched = isMatched;
    }
}
