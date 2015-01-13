package com.elster.jupiter.demo.impl.finders;

import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.model.ComSchedule;

import javax.inject.Inject;

public class ComScheduleFinder extends NamedFinder<ComScheduleFinder, ComSchedule>{
    private final SchedulingService schedulingService;

    @Inject
    public ComScheduleFinder(SchedulingService schedulingService) {
        super(ComScheduleFinder.class);
        this.schedulingService = schedulingService;
    }

    @Override
    public ComSchedule find() {
        return schedulingService.findAllSchedules().stream().filter(sch -> sch.getName().equals(getName())).findFirst().orElseThrow(getFindException());
    }
}
