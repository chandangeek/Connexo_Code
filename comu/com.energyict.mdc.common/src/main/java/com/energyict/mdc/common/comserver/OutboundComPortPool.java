/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.comserver;

import com.elster.jupiter.time.TimeDuration;

import java.util.List;

/**
 * Models a collection of {@link OutboundComPort}s with similar characteristics
 * and primarily serves the purpose to optimize communication with devices
 * and which OutboundComPort is used for that communication.
 * Instead of linking a device (or rather its ConnectionTask)
 * to a specific OutboundComPort, it is linked to a ComPortPool
 * and will then use the first free available OutboundComPort in that pool
 * when communication with the device is required.
 * To maximize scalability it is therefore allowed that
 * ComPorts are part of multiple pools at the same time.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-26 (09:14)
 */
public interface OutboundComPortPool extends ComPortPool {

    /**
     * Gets the amount of time that any ComTaskExecution
     * is allowed to execute before it times out and removed from the
     * active execution stack.
     *
     * @return The amount of time
     */
    public TimeDuration getTaskExecutionTimeout ();
    public void setTaskExecutionTimeout (TimeDuration duration);

    /**
    * Gets the list of {@link OutboundComPort} available through this pool.
    *
    * @return The list of OutboundComPorts
    */
    public List<OutboundComPort> getComPorts();

    public void addOutboundComPort(OutboundComPort outboundComPort);

    public void removeOutboundComPort(OutboundComPort outboundComPort);

}