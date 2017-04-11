/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.mdm.common.rest.IntervalInfo;

import java.math.BigDecimal;
import java.util.List;

public class ValueCorrectionInfo {
    public BigDecimal amount;
    public String estimationComment;
    public List<IntervalInfo> intervals;
    public boolean onlySuspectOrEstimated;
    public String projected;
    public String type;
}
