package com.energyict.mdc.multisense.api.impl;


import com.energyict.mdc.device.data.tasks.TaskStatus;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.Instant;

public class ComTaskExecutionInfo extends LinkInfo {
    @NotNull
    public ComTaskExecutionType type;
    public LinkInfo connectionTask;
    public LinkInfo schedule;
    public LinkInfo device;
    @NotNull
    public Instant nextExecution;
    public Instant plannedNextExecution;
    public Integer priority;
    public Instant lastCommunicationStart;
    @XmlJavaTypeAdapter(TaskStatusAdapter.class)
    public TaskStatus status;
    public Instant lastSuccessfulCompletion;
    public TemporalExpressionInfo schedulingSpec;
    @NotNull
    public LinkInfo comTask;
}
