/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.rest;

import com.elster.jupiter.cbo.MacroPeriod;

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
        register("Yearly", MacroPeriod.YEARLY);
    }
}
