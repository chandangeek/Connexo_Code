/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.demo.impl.Log;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

public class ComScheduleBuilder extends NamedBuilder<ComSchedule, ComScheduleBuilder>{
    private final SchedulingService schedulingService;

    private List<ComTask> comTasks;
    private TimeDuration every;

    @Inject
    public ComScheduleBuilder(SchedulingService schedulingService) {
        super(ComScheduleBuilder.class);
        this.schedulingService = schedulingService;
    }

    public ComScheduleBuilder withComTasks(List<ComTask> comTasks){
        this.comTasks = comTasks;
        return this;
    }

    public ComScheduleBuilder withTimeDuration(TimeDuration timeDuration){
        this.every = timeDuration;
        return this;
    }

    @Override
    public Optional<ComSchedule> find(){
        return schedulingService.getAllSchedules().stream().filter(cs -> cs.getName().equals(getName())).findFirst();
    }

    @Override
    public ComSchedule create(){
        Log.write(this);
        LocalDateTime startOn = LocalDateTime.now();
        startOn = startOn.withSecond(0).withMinute(0).withHour(0);
        com.energyict.mdc.scheduling.model.ComScheduleBuilder comScheduleBuilder = schedulingService.newComSchedule(getName(), new TemporalExpression(every), startOn.toInstant(ZoneOffset.UTC));
        if (comTasks!= null){
            for (ComTask comTask : comTasks) {
                comScheduleBuilder.addComTask(comTask);
            }
        }
        return comScheduleBuilder.build();
    }
}
