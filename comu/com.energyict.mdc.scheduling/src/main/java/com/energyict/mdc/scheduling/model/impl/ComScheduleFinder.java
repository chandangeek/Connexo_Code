package com.energyict.mdc.scheduling.model.impl;

import com.energyict.mdc.common.CanFindByLongPrimaryKey;
import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.model.ComSchedule;

import java.util.Optional;

public class ComScheduleFinder implements CanFindByLongPrimaryKey<ComSchedule> {

    private final SchedulingService schedulingService;

    public ComScheduleFinder(SchedulingService schedulingService) {
        this.schedulingService = schedulingService;
    }

    @Override
    public FactoryIds factoryId() {
        return FactoryIds.COMSCHEDULE;
    }

    @Override
    public Class<ComSchedule> valueDomain() {
        return ComSchedule.class;
    }

    @Override
    public Optional<ComSchedule> findByPrimaryKey(long id) {
        return schedulingService.findSchedule(id);
    }
}
