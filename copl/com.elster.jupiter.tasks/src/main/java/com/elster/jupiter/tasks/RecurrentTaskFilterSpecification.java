/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.tasks;

import com.elster.jupiter.util.conditions.Order;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by igh on 6/11/2015.
 */
public class RecurrentTaskFilterSpecification {

    public Set<String> applications = new HashSet<>();
    public Set<String> queues = new HashSet<>();
    public Set<String> queueTypes = new HashSet<>();
    public Instant startedOnFrom;
    public Instant startedOnTo;
    public Set<String> suspended = new HashSet<>();
    public Instant nextExecutionFrom;
    public Instant nextExecutionTo;
    public Integer priorityFrom;
    public Integer priorityTo;
    public List<Order> sortingColumns;
}
