/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public class ChannelHistoryDataInfo extends ChannelDataInfo {

    @JsonProperty("userName")
    public String userName;
    @JsonProperty("journalTime")
    public Instant journalTime;
    @JsonProperty("isActive")
    public boolean isActive;
    @JsonProperty("version")
    public long version;

    public ChannelHistoryDataInfo(ChannelDataInfo channelDataInfo) {
        this.interval = channelDataInfo.interval;
        this.readingTime = channelDataInfo.readingTime;
        this.validationActive = channelDataInfo.validationActive;
        this.readingQualities = channelDataInfo.readingQualities;
        this.multiplier = channelDataInfo.multiplier;
        this.value = channelDataInfo.value;
        this.reportedDateTime = channelDataInfo.reportedDateTime;
        this.mainValidationInfo = channelDataInfo.mainValidationInfo;
        this.bulkValidationInfo = channelDataInfo.bulkValidationInfo;
        this.slaveChannel = channelDataInfo.slaveChannel;
        this.dataValidated = channelDataInfo.dataValidated;
        this.collectedValue = channelDataInfo.collectedValue;
        this.isBulk = channelDataInfo.isBulk;
    }
}







