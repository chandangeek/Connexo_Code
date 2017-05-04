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

    public OutputChannelHistoryDataInfo(OutputChannelDataInfo channelDataInfo) {
        this.interval = channelDataInfo.interval;
        this.value = channelDataInfo.value;
        this.calculatedValue = channelDataInfo.calculatedValue;
        this.reportedDateTime = channelDataInfo.reportedDateTime;
        this.dataValidated = channelDataInfo.dataValidated;
        this.isConfirmed = channelDataInfo.isConfirmed;
        this.validationResult = channelDataInfo.validationResult;
        this.action = channelDataInfo.action;
        this.validationRules = channelDataInfo.validationRules;
        this.modificationFlag = channelDataInfo.modificationFlag;
        this.modificationDate = channelDataInfo.modificationDate;
        this.editedInApp = channelDataInfo.editedInApp;
        this.readingQualities = channelDataInfo.readingQualities;
        this.estimatedByRule = channelDataInfo.estimatedByRule;
        this.isProjected = channelDataInfo.isProjected;
        this.ruleId = channelDataInfo.ruleId;
    }
}
