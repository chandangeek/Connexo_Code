/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ValidationLoadProfilePeriodInfo {
        @JsonProperty("id")
        public Long id;
        @JsonProperty("intervalStart")
        public Long startInterval;
        @JsonProperty("intervalEnd")
        public Long endInterval;

        public ValidationLoadProfilePeriodInfo() {}
    }
