package com.energyict.mdc.scheduling.model.impl;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;

public class ComTaskInComScheduleImpl implements ComTaskInComSchedule {

    enum Fields {
        COM_SCHEDULE_REFERENCE("comScheduleReference"),
        COM_TASK_REFERENCE("comTaskReference");
        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }
    private Reference<ComSchedule> comScheduleReference = ValueReference.absent();
    private Reference<ComTask> comTaskReference = ValueReference.absent();

    public ComTaskInComScheduleImpl() {
    }

    public ComTaskInComScheduleImpl(ComSchedule comSchedule, ComTask comTask) {
        this.comScheduleReference.set(comSchedule);
        this.comTaskReference.set(comTask);
    }

    @Override
    public ComSchedule getComSchedule() {
        return comScheduleReference.get();
    }

    @Override
    public void setComSchedule(ComSchedule comSchedule) {
        this.comScheduleReference.set(comSchedule);
    }

    @Override
    public ComTask getComTask() {
        return comTaskReference.get();
    }

    @Override
    public void setComTask(ComTask comTask) {
        this.comTaskReference.set(comTask);
    }
}
