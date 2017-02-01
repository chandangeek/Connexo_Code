/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.time.rest.PeriodicalExpressionInfo;

import java.time.Instant;

public class EstimationTaskShortInfo {
    public long id;
    public String name;
    public String recurrence;
    public PeriodicalExpressionInfo schedule;
    public Instant nextRun;
}
