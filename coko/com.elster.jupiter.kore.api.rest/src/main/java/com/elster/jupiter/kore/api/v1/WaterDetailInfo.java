/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.v1;

import com.elster.jupiter.metering.BypassStatus;
import com.elster.jupiter.util.YesNoAnswer;
import com.elster.jupiter.util.units.Quantity;

/**
 * Created by bvn on 4/11/16.
 */
public class WaterDetailInfo extends DefaultDetailInfo {
    public Quantity pressure;
    public Quantity physicalCapacity;
    public YesNoAnswer limiter;
    public String loadLimiterType;
    public Quantity loadLimit;
    public YesNoAnswer bypass;
    public BypassStatus bypassStatus;
    public YesNoAnswer valve;
    public YesNoAnswer capped;
    public YesNoAnswer clamped;


}
