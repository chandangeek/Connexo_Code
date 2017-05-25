/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;


import com.elster.jupiter.rest.util.BigDecimalFunction;
import com.energyict.mdc.common.rest.IntervalInfo;

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
    public boolean projected;
    @JsonProperty("type")
    public BigDecimalFunction type;
}
