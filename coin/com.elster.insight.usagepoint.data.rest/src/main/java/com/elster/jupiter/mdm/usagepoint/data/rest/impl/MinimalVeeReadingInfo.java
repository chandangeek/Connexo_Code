package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MinimalVeeReadingInfo {
    @JsonProperty("dataValidated")
    public Boolean dataValidated;

    @JsonProperty("mainValidationInfo")
    public MinimalVeeReadingValueInfo mainValidationInfo = new MinimalVeeReadingValueInfo();

    @JsonProperty("bulkValidationInfo")
    public MinimalVeeReadingValueInfo bulkValidationInfo = new MinimalVeeReadingValueInfo();

    public MinimalVeeReadingInfo() {
    }
}

