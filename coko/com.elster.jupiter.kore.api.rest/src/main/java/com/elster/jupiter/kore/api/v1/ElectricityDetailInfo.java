package com.elster.jupiter.kore.api.v1;

import com.elster.jupiter.cbo.PhaseCode;
import com.elster.jupiter.util.YesNoAnswer;
import com.elster.jupiter.util.units.Quantity;

/**
 * Created by bvn on 4/11/16.
 */
public class ElectricityDetailInfo extends DefaultDetailInfo {
    public Quantity nominalServiceVoltage;
    public PhaseCode phaseCode;
    public Quantity ratedCurrent;
    public Quantity ratedPower;
    public Quantity estimatedLoad;
    public YesNoAnswer limiter;
    public String loadLimiterType;
    public Quantity loadLimit;
    public YesNoAnswer interruptible;

}
