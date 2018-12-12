/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.metering.ServiceKind;
import com.energyict.mdc.common.rest.MapBasedXmlAdapter;

public class ServiceKindAdapter extends MapBasedXmlAdapter<ServiceKind> {

    public ServiceKindAdapter() {
        register("", null);
        register("Other", ServiceKind.OTHER);
        register("Electricity", ServiceKind.ELECTRICITY);
        register("Gas", ServiceKind.GAS);
        register("Heat", ServiceKind.HEAT);
        register("Internet", ServiceKind.INTERNET);
        register("Rates", ServiceKind.RATES);
        register("Water", ServiceKind.WATER);
        register("Tv license", ServiceKind.TVLICENSE);
        register("Time", ServiceKind.TIME);
        register("Refuse", ServiceKind.REFUSE);
        register("Sewerage", ServiceKind.SEWERAGE);
    }
}
