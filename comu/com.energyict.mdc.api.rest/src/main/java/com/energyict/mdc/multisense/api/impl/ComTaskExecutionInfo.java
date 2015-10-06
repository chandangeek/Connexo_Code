package com.energyict.mdc.multisense.api.impl;


import com.energyict.mdc.device.data.tasks.TaskStatus;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.Instant;
import java.util.List;

public class ComTaskExecutionInfo extends LinkInfo {
    public LinkInfo device;
    public List<LinkInfo> comTasks;
    public LinkInfo schedule;
    public Instant nextExecution;
    public Instant plannedNextExecution;
    public int priority;
    public ComTaskExecutionType type;
    public Instant lastCommunicationStart;
    @XmlJavaTypeAdapter(TaskStatusAdapter.class)
    public TaskStatus status;
    public Boolean isOnHold;
    public Instant lastSuccessfulCompletion;
}
