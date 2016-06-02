package com.elster.jupiter.kore.api.impl;

import com.elster.jupiter.metering.BypassStatus;
import com.elster.jupiter.util.YesNoAnswer;
import com.elster.jupiter.util.units.Quantity;

/**
 * Created by bvn on 4/11/16.
 */
public class HeatDetailInfo extends DefaultDetailInfo {
    public Quantity pressure;
    public Quantity physicalCapacity;
    public YesNoAnswer bypass;
    public BypassStatus bypassStatus;
    public YesNoAnswer valve;


}
