package com.energyict.mdc.scheduling.model;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface ComScheduleBuilder {
    ComScheduleBuilder mrid(String mrid);
    ComSchedule build();
}
