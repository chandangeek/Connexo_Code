/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.comserver;

/**
 * Models a {@link ComServer} that is capable of setting up outbound connections.<br>
 * Will additionally detect the following when polling for changes:
 * <ul>
 * <li>Adding ComPorts</li>
 * </ul>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-03-27 (17:39)
 */
public interface OutboundCapableComServer extends ComServer, OutboundCapable {

    boolean supportsExecutionOfHighPriorityComTasks();


    /**
     * Finds and returns all high priority {@link ComJob}s
     * that are ready to be executed by one of the {@link OutboundComPort}s
     * of this OutboundCapableComServer.<br/>
     * <b>Note:</b> The actual load of high priority tasks (~ the number of high priority tasks which are currently executed)
     * mapped per {@link ComPortPool} is also provided. This information can be used to determine
     * the maximum number of additional high priority tasks which can be picked up per {@link ComPortPool}.
     *
     * @param currentHighPriorityLoadPerComPortPool A map containing the number of the high priority tasks which are currently executed per ComPortPool
     * @return The List of ComJob
     */
//    List<HighPriorityComJob> findExecutableHighPriorityComTasks(Map<Integer, Integer> currentHighPriorityLoadPerComPortPool);

    /**
     * Finds and returns all high priority {@link ComJob}s
     * that will be ready to be executed by one of the {@link OutboundComPort}s
     * of this OutboundCapableComServer on the specified Date.<br/>
     * <b>Note:</b> The actual load of high priority tasks (~ the number of high priority tasks which are currently executed)
     * mapped per {@link ComPortPool} is also provided. This information can be used to determine
     * the maximum number of additional high priority tasks which can be picked up per {@link ComPortPool}.
     *
     * @param currentHighPriorityLoadPerComPortPool A map containing the number of the high priority tasks which are currently executed per ComPortPool
     * @return The List of ComJob
     */
//    List<HighPriorityComJob> findExecutableHighPriorityComTasks(Map<Integer, Integer> currentHighPriorityLoadPerComPortPool, Date date);
}