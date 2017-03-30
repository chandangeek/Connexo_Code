/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;


import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;
import com.energyict.mdc.device.data.tasks.TaskStatus;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.Instant;

public class ComTaskExecutionInfo extends LinkInfo<Long> {
    @NotNull
    public ComTaskExecutionType type;
    public LinkInfo<Long> connectionTask;
    public LinkInfo<Long> schedule;
    public LinkInfo<Long> device;
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
    public LinkInfo<Long> comTask;
}
