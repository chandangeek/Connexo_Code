/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;


import com.energyict.mdc.common.rest.IntervalInfo;

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
