package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointDetailBuilder;
import com.elster.jupiter.util.YesNoAnswer;

import java.time.Clock;

/**
 * Created by bvn on 4/11/16.
 */
public abstract class UsagePointTechnicalInfo {
    public YesNoAnswer grounded;
    public YesNoAnswer collar;

    abstract UsagePointDetailBuilder createDetail(UsagePoint usagePoint, Clock clock);
}
