/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.rest.status.impl;

/**
 * Accepted criteria for connections and communication filters
 * Created by bvn on 9/5/14.
 */
enum FilterOption {
    currentStates,
    latestStates,
    latestResults,
    comTasks,
    comSchedules,
    startIntervalFrom,
    startIntervalTo,
    finishIntervalFrom,
    finishIntervalTo,
    connectionTypes,
    deviceTypes,
    comPortPools,
    deviceGroups;
}
