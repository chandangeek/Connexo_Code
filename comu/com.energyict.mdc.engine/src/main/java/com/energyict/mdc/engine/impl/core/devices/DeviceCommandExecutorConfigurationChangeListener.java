/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core.devices;


/**
 * Models the behavior of a component that will listen
 * for changes to the configuration of a DeviceCommandExecutor.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-08-22 (10:20)
 */
interface DeviceCommandExecutorConfigurationChangeListener {

    /**
     * Applies the changed capacity of the store task queue.
     *
     * @param newCapacity The new capacity
     */
    void changeQueueCapacity(int newCapacity);

    /**
     * Applies the changed number of threads that execute
     * store task from the queue.
     *
     * @param newNumberOfThreads The new number of threads
     */
    void changeNumberOfThreads(int newNumberOfThreads);

    /**
     * Applies the changed thread priority to all running threads
     * @param newPriority The new priority
     */
    void changeThreadPriority(int newPriority);

}