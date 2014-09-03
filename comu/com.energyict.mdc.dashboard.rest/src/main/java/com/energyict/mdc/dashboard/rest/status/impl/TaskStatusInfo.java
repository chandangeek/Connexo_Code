package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.rest.TaskStatusAdapter;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Created by bvn on 8/12/14.
 */
class TaskStatusInfo {
    private static final TaskStatusAdapter TASK_STATUS_ADAPTER = new TaskStatusAdapter();
    @XmlJavaTypeAdapter(TaskStatusAdapter.class)
    public TaskStatus id;
    public String displayValue;

    TaskStatusInfo(TaskStatus taskStatus, Thesaurus thesaurus) throws Exception {
        this.id=taskStatus;
        this.displayValue=thesaurus.getString(TASK_STATUS_ADAPTER.marshal(taskStatus),TASK_STATUS_ADAPTER.marshal(taskStatus));
    }
}
