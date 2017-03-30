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
    public Instant startIntervalFrom;
    public Instant startIntervalTo;
    public Instant finishIntervalFrom;
    public Instant finishIntervalTo;
}
