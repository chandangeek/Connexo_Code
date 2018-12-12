/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.tasks;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by igh on 6/11/2015.
 */
public class RecurrentTaskFilterSpecification {

    public Set<String> applications = new HashSet<>();
    public Set<String> queues = new HashSet<>();
    public Instant startedOnFrom;
    public Instant startedOnTo;
}
