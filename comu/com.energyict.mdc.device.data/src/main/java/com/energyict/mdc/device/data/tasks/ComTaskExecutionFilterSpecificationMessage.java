/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.tasks;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * Simple structure to hold all IDs so we can store the filter in the DB
 */
public class ComTaskExecutionFilterSpecificationMessage {

    public Set<Long> deviceGroups = new HashSet<>();
    public Set<String> currentStates = new HashSet<>();
    public Set<String> latestResults = new HashSet<>();
    public Set<Long> comSchedules = new HashSet<>();
    public Set<Long> comTasks = new HashSet<>();
    public Set<Long> deviceTypes = new HashSet<>();
    //CONM-2507
    public Set<String> connectionMethods = new HashSet<>();
    public String device;
    //CONM-2553
    public String strtTo;
    public String strtFrom;
    public String finishTo;
    public String finishFrom;
    public Instant startIntervalFrom = (strtFrom != null) ? Instant.parse( strtFrom) : null;
    public Instant startIntervalTo = (strtTo != null) ? Instant.parse(strtTo) : null;
    public Instant finishIntervalFrom = (finishFrom != null) ? Instant.parse(finishFrom) : null;
    public Instant finishIntervalTo = (finishTo != null) ? Instant.parse(finishTo) : null;
}
