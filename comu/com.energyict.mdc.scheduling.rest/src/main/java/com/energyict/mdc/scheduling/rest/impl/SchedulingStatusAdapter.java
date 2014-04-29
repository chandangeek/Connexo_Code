package com.energyict.mdc.scheduling.rest.impl;

import com.energyict.mdc.common.rest.MapBasedXmlAdapter;
import com.energyict.mdc.scheduling.model.SchedulingStatus;

public class SchedulingStatusAdapter extends MapBasedXmlAdapter<SchedulingStatus> {

    public SchedulingStatusAdapter() {
        register("", null);
        register("Paused", SchedulingStatus.PAUSED);
        register("Active", SchedulingStatus.ACTIVE);
    }
}
