/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.monitor;

import aQute.bnd.annotation.ProviderType;

/**
 * Models the statistics of the data collection storage process
 * that are gathered by the process that monitors a
 * RunningComServer.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-03 (11:16)
 */
@ProviderType
public interface CollectedDataStorageStatistics {

    /**
     * Gets the maximum capacity of queue that contains
     * the CollectedData.
     *
     * @return The maximum capacity of the CollectedData queue
     */
    int getCapacity();

    /**
     * Gets the current number of CollectedData
     * elements on the queue waiting to be processed.
     *
     * @return The current number of CollectedData elements on the queue
     */
    int getCurrentSize();

    /**
     * Gets the current load of the data collection storage process
     * as a percentage. The load is defined as current size / capacity.
     * As an example, when the capacity is 100 and the current size
     * is 37 the the load is 37.
     *
     * @return The load as percentage
     */
    int getLoadPercentage();

    /**
     * Gets the number of threads that are consuming
     * CollectedData
     * elements from the queue.
     *
     * @return The number of threads that consume CollectedData elements from the queue
     */
    int getNumberOfThreads();

    /**
     * Gets the priority of the threads that are consuming
     * CollectedData
     * elements from the queue.
     * This number will in the range specified by the Thread class,
     * i.e. Thread.MIN_PRIORITY <= priority <= Thread.MAX_PRIORITY.
     *
     * @return The priority of the threads that consume CollectedData elements
     */
    int getThreadPriority();

    /**
     * Gets a comma separated list of the name of the threads
     * that currently have acquired tokens. The number of tokens
     * they have acquired is printed in brackets if &gt; 1.
     * It is not expected that one thread acquires more than 1
     * token at a time so if you see tokens counts printed
     * then that is a sign that tokens are not correctly
     * being returned as a result of failures.
     *
     * @return The comma separated list of thread names
     */
    String getThreadNames();

}