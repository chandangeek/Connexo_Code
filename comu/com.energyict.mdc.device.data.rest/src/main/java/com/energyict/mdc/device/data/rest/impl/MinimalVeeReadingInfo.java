/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MinimalVeeReadingInfo {
    @JsonProperty("dataValidated")
    public Boolean dataValidated;

    @JsonProperty("validationActive")
    public Boolean validationStatus;

    @JsonProperty("mainValidationInfo")
    public MinimalVeeReadingValueInfo mainValidationInfo = new MinimalVeeReadingValueInfo();

    @JsonProperty("bulkValidationInfo")
    public MinimalVeeReadingValueInfo bulkValidationInfo = new MinimalVeeReadingValueInfo();

}