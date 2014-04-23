package com.energyict.mdc.scheduling.model.impl;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.scheduling.NextExecutionSpecs;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.scheduling.model.SchedulingStatus;
import com.energyict.mdc.tasks.ComTask;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ComScheduleImpl implements ComSchedule {

    enum Fields {
        NAME("name"),
        MOD_DATE("mod_date"),
        NEXT_EXECUTION_SPEC("nextExecutionSpec"),
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
    private long id;
    private String name;
    private List<ComTaskInComSchedule> comTaskUsages = new ArrayList<>();
    private Reference<NextExecutionSpecs> nextExecutionSpec = ValueReference.absent();
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

    @Override
    public NextExecutionSpecs getNextExecutionSpec() {
        return nextExecutionSpec.get();
    }

    @Override
    public void setNextExecutionSpec(NextExecutionSpecs nextExecutionSpec) {
        this.nextExecutionSpec.set(nextExecutionSpec);
    }

    public List<ComTask> getComTasks() {
        List<ComTask> comTasks = new ArrayList<>();
        for (ComTaskInComSchedule comTaskUsage : comTaskUsages) {
            comTasks.add(comTaskUsage.getComTask());
        }
        return comTasks;
    }

    public void addComTask(ComTask comTask) {
        // TODO  verify that all devices that are already linked to the ComSchedule have that ComTask enabled.
        comTaskUsages.add(new ComTaskInComScheduleImpl(this, comTask));
    }

    public SchedulingStatus getSchedulingStatus() {
        return schedulingStatus;
    }
}
