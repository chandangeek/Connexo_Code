package com.elster.jupiter.metering.impl.aggregation;

/**
 * Created by igh on 20/06/2016.
 */
public enum ReadingQuality {

    DERIVED_DETERMINISTIC("3.11.0"),
    DERIVED_INDETERMINISTIC("3.11.1"),  //edited/estimated
    DERIVED_SUSPECT("3.11.1000"),
    DERIVED_MISSING("3.11.1001");

    private String code;

    ReadingQuality(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }



}
