/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public class OutputChannelHistoryDataInfo extends OutputChannelDataInfo {

    @JsonProperty("userName")
    public String userName;
    @JsonProperty("journalTime")
    public Instant journalTime;
    @JsonProperty("isActive")
    public boolean isActive;
    @JsonProperty("version")
    public long version;

    public OutputChannelHistoryDataInfo() {
    }
}
