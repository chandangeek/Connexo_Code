package com.energyict.mdc.engine.model;

import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.shadow.ports.OutboundComPortPoolShadow;
import com.energyict.mdc.tasks.ConnectionTask;
import java.util.List;

/**
 * Models a collection of {@link com.energyict.mdc.engine.model.OutboundComPort}s with similar characteristics
 * and primarily serves the purpose to optimize communication with devices
 * and which OutboundComPort is used for that communication.
 * Instead of linking a device (or rather its {@link ConnectionTask})
 * to a specific OutboundComPort, it is linked to a ComPortPool
 * and will then use the first free available OutboundComPort in that pool
 * when communication with the device is required.
 * To maximize scalability it is therefore allowed that
 * ComPorts are part of multiple pools at the same time.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-26 (09:14)
 */
public interface OutboundComPortPool extends ComPortPool<OutboundComPortPoolShadow> {

    /**
     * Gets a {@link OutboundComPortPoolShadow shadow}
     * that allows to make to this OutboundComPortPool.
     *
     * @return The OutboundComPortPoolShadow
     */
    public OutboundComPortPoolShadow getShadow();

    /**
     * Gets the amount of time that any {@link com.energyict.mdc.tasks.ComTaskExecution}
     * is allowed to execute before it times out and removed from the
     * active execution stack.
     *
     * @return The amount of time
     */
    public TimeDuration getTaskExecutionTimeout ();

    /**
    * Gets the list of {@link com.energyict.mdc.engine.model.OutboundComPort} available through this pool.
    *
    * @return The list of OutboundComPorts
    */
    public List<OutboundComPort> getComPorts();

}