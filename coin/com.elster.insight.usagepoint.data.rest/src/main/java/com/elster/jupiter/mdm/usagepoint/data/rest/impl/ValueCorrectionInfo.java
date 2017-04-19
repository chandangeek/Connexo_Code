/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.mdm.common.rest.IntervalInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

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
    public String projected;
    @JsonProperty("type")
    public String type;
}
