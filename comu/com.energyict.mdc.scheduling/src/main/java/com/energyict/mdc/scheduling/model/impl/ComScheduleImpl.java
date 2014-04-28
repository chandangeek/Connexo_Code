package com.energyict.mdc.scheduling.model.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.scheduling.NextExecutionSpecs;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.TemporalExpression;
import com.energyict.mdc.scheduling.events.DeleteEventType;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.scheduling.model.SchedulingStatus;
import com.energyict.mdc.tasks.ComTask;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.inject.Inject;

@UniqueName
public class ComScheduleImpl implements ComSchedule {

    private final SchedulingService schedulingService;
    private final EventService eventService;
    private final DataModel dataModel;

    enum Fields {
        NAME("name"),
        MOD_DATE("mod_date"),
        NEXT_EXECUTION_SPEC("nextExecutionSpecs"),
        STATUS("schedulingStatus"),
        COM_TASK_IN_COM_SCHEDULE("comTaskUsages");
        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

    @Inject
    public ComScheduleImpl(SchedulingService schedulingService, EventService eventService,DataModel dataModel) {
        this.schedulingService = schedulingService;
        this.eventService = eventService;
        this.dataModel = dataModel;
    }

    private long id;
    private String name;
    private List<ComTaskInComSchedule> comTaskUsages = new ArrayList<>();
    private Reference<NextExecutionSpecs> nextExecutionSpecs = ValueReference.absent();
    private SchedulingStatus schedulingStatus;
    private Date mod_date;

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    // Intentionally not on interface
    public NextExecutionSpecs getNextExecutionSpecs() {
        return nextExecutionSpecs.get();
    }

    @Override
    public TemporalExpression getTemporalExpression() {
        return this.nextExecutionSpecs.get().getTemporalExpression();
    }

    @Override
    public void setTemporalExpression(TemporalExpression temporalExpression) {
        if (!this.nextExecutionSpecs.isPresent()) {
            NextExecutionSpecs nextExecutionSpecs = schedulingService.newNextExecutionSpecs(temporalExpression);
            nextExecutionSpecs.save();
            this.nextExecutionSpecs.set(nextExecutionSpecs);
        } else  {
            this.nextExecutionSpecs.get().setTemporalExpression(temporalExpression);
            this.nextExecutionSpecs.get().save();
        }
    }

    @Override
    public void save() {
        Save.action(this.getId()).save(dataModel, this);
    }

    @Override
    public void delete() {
        this.eventService.postEvent(DeleteEventType.COM_SCHEDULE.topic(), this);

//        for (ComTaskExecution comTaskExecution : this.deviceDataService.findComTaskExecutionsByComSchedule(this)) {
//            comTaskExecution.getDevice().getComTaskExecutionUpdater(comTaskExecution).comSchedule(null).removeNextExecutionSpec().update();
//        }
        this.dataModel.remove(this);
    }

    @Override
    public List<ComTask> getComTasks() {
        List<ComTask> comTasks = new ArrayList<>();
        for (ComTaskInComSchedule comTaskUsage : comTaskUsages) {
            comTasks.add(comTaskUsage.getComTask());
        }
        return comTasks;
    }

    @Override
    public void addComTask(ComTask comTask) {
        // TODO  verify that all devices that are already linked to the ComSchedule have that ComTask enabled.
        comTaskUsages.add(new ComTaskInComScheduleImpl(this, comTask));
    }

    @Override
    public SchedulingStatus getSchedulingStatus() {
        return schedulingStatus;
    }

    public void setSchedulingStatus(SchedulingStatus schedulingStatus) {
        this.schedulingStatus = schedulingStatus;
    }
}
