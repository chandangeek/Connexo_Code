/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.mdm.common.rest.IntervalInfo;
import com.elster.jupiter.rest.util.BigDecimalFunction;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown=true)
public class ValueCorrectionInfo {
    @JsonProperty("amount")
    public BigDecimal amount;
    @JsonProperty("commentId")
    public long commentId;
    @JsonProperty("intervals")
    public List<IntervalInfo> intervals;
    @JsonProperty("onlySuspectOrEstimated")
    public boolean onlySuspectOrEstimated;
    @JsonProperty("projected")
    public boolean projected;
    @JsonProperty("type")
    public BigDecimalFunction type;

    public List<OutputChannelDataInfo> editedReadings;
}
