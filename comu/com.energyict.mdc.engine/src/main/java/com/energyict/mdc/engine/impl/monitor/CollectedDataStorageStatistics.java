package com.energyict.mdc.engine.impl.monitor;

/**
 * Models the statistics of the data collection storage process
 * that are gathered by the process that monitors a
 * RunningComServer.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-03 (11:16)
 */
public interface CollectedDataStorageStatistics {

    /**
     * Gets the maximum capacity of queue that contains
     * the CollectedData.
     *
     * @return The maximum capacity of the CollectedData queue
     */
    public int getCapacity ();

    /**
     * Gets the current number of CollectedData
     * elements on the queue waiting to be processed.
     *
     * @return The current number of CollectedData elements on the queue
     */
    public int getCurrentSize ();

    /**
     * Gets the current load of the data collection storage process
     * as a percentage. The load is defined as current size / capacity.
     * As an example, when the capacity is 100 and the current size
     * is 37 the the load is 37.
     *
     * @return The load as percentage
     */
    public int getLoadPercentage ();

    /**
     * Gets the number of threads that are consuming
     * CollectedData
     * elements from the queue.
     *
     * @return The number of threads that consume CollectedData elements from the queue
     */
    public int getNumberOfThreads ();

    /**
     * Gets the priority of the threads that are consuming
     * CollectedData
     * elements from the queue.
     * This number will in the range specified by the Thread class,
     * i.e. Thread.MIN_PRIORITY <= priority <= Thread.MAX_PRIORITY.
     *
     * @return The priority of the threads that consume CollectedData elements
     */
    public int getThreadPriority ();

}