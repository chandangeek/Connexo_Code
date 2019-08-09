/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.device.data.impl.tasks.ComTaskExecutionImpl;
import com.energyict.mdc.device.data.impl.tasks.ScheduledConnectionTaskImpl;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides an implementation for the {@link ComJob} interface
 * that represents a group of {@link ComTaskExecution}s that need to be
 * executed one after the other.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-07-20 (14:06)
 */
@XmlRootElement
public class ComTaskExecutionGroup implements ComJob {

    private ConnectionTask connectionTask;
    private List<ComTaskExecution> comTaskExecutions = new ArrayList<>();
    private long connectionTaskId;

    // For xml serialization purposes only
    public ComTaskExecutionGroup() {
        super();
    }

    public ComTaskExecutionGroup(ConnectionTask connectionTask) {
        this();
        this.connectionTask = connectionTask;
    }

    @Override
    @XmlElement(type = ScheduledConnectionTaskImpl.class, name = "connectionTask")
    public ConnectionTask getConnectionTask() {
        return connectionTask;
    }

    @Override
    @XmlAttribute
    public long getConnectionTaskId() {
        connectionTaskId = connectionTask.getId();
        return connectionTaskId;
    }

    public void add(ComTaskExecution comTask) {
        this.comTaskExecutions.add(comTask);
    }

    @Override
    @XmlElement(type = ComTaskExecutionImpl.class, name = "comTaskExecutions")
    public List<ComTaskExecution> getComTaskExecutions() {
        List<ComTaskExecution> comTaskExecutions = new ArrayList<>();
        comTaskExecutions.addAll(this.comTaskExecutions);
        return comTaskExecutions;
    }

    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    public void setXmlType(String ignore){
        // for serialization
    }
}