package com.energyict.mdc.multisense.api.impl;


import com.energyict.mdc.device.data.tasks.TaskStatus;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.Instant;
import java.util.List;

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
    public Boolean useDefaultConnectionTask;
    public Boolean ignoreNextExecutionSpecForInbound;
    @XmlJavaTypeAdapter(TaskStatusAdapter.class)
    public TaskStatus status;
    public Instant lastSuccessfulCompletion;
    public TemporalExpressionInfo schedulingSpec;
    @NotNull
    public LinkInfo comTask;
}
