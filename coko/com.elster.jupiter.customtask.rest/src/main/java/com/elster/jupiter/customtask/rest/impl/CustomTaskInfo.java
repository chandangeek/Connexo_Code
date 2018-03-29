/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.customtask.rest.impl;

import com.elster.jupiter.time.rest.PeriodicalExpressionInfo;

import javax.xml.bind.annotation.XmlRootElement;
import java.time.Instant;
import java.util.List;
import java.util.logging.Level;

@XmlRootElement
public class CustomTaskInfo {

    public long id;
    public String name;
    public long version;
    public boolean active;
    public int logLevel = Level.WARNING.intValue();
    public CustomTaskHistoryMinimalInfo lastOccurrence;
    public PeriodicalExpressionInfo schedule;
    public String type;
    public String recurrence;
    public Instant nextRun;
    public Instant lastRun;
    public List<TaskInfo> nextRecurrentTasks;
    public List<TaskInfo> previousRecurrentTasks;
    public List<CustomTaskPropertiesInfo> properties;


}
