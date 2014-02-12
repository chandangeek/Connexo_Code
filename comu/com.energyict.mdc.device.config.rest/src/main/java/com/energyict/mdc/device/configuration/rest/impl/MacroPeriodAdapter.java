package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.cbo.MacroPeriod;
import com.energyict.mdc.common.rest.MapBasedXmlAdapter;

public class MacroPeriodAdapter extends MapBasedXmlAdapter<MacroPeriod> {

    public MacroPeriodAdapter() {
        register("", MacroPeriod.NOTAPPLICABLE);
        register("Not applicable", MacroPeriod.NOTAPPLICABLE);
        register("Billing period", MacroPeriod.BILLINGPERIOD);
        register("Daily", MacroPeriod.DAILY);
        register("Monthly", MacroPeriod.MONTHLY);
        register("Seasonal", MacroPeriod.SEASONAL);
        register("Specified period", MacroPeriod.SPECIFIEDPERIOD);
        register("Weekly", MacroPeriod.WEEKLYS);
    }
}
