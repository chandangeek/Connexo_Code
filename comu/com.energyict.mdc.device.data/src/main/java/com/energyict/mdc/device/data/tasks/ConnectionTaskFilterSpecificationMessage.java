package com.energyict.mdc.device.data.tasks;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * Simple structure to hold all IDs so we can store the filter in the DB (this is the actual message passed to the queue
 * Created by bvn on 3/27/15.
 */
public class ConnectionTaskFilterSpecificationMessage {

    public Set<String> currentStates = new HashSet<>();
    public Set<Long> comPortPools = new HashSet<>();
    public Set<Long> connectionTypes = new HashSet<>();
    public Set<String> latestResults = new HashSet<>();
    public Set<String> latestStates = new HashSet<>();
    public Set<Long> deviceTypes = new HashSet<>();
    public Set<Long> deviceGroups = new HashSet<>();
    public Instant startIntervalFrom;
    public Instant startIntervalTo;
    public Instant finishIntervalFrom;
    public Instant finishIntervalTo;
}
