package com.energyict.mdc.engine.monitor;

import com.energyict.mdc.engine.impl.core.ComPortListener;
import com.energyict.mdc.engine.impl.core.RunningComServer;
import com.energyict.mdc.engine.impl.core.ScheduledComPort;
import com.energyict.mdc.engine.model.OnlineComServer;

/**
 * Provides factory services for management beans (aka MBean)
 * that provide monitoring information of running ComServer components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-03 (13:00)
 */
public interface ManagementBeanFactory {

    /**
     * Finds or creates the {@link ComServerMonitorImplMBean}
     * for the specified RunningComServer.
     *
     * @param runningComServer The RunningComServer
     * @return The ComServerMonitorImplMBean
     */
    public ComServerMonitorImplMBean findOrCreateFor (RunningComServer runningComServer);

    /**
     * Finds or creates the {@link ComServerMonitorImplMBean}
     * for the specified {@link OnlineComServer}.
     *
     * @param onlineComServer The OnlineComServer
     * @return The ComServerMonitorImplMBean
     */
    public ComServerMonitorImplMBean findFor(OnlineComServer onlineComServer);

    /**
     * Removes the {@link ComServerMonitorImplMBean}
     * for the specified {@link RunningComServer}
     * if it already exists and does nothing when
     * the ComServerMonitorImplMBean does not exist.
     *
     * @param runningComServer The RunningComServer
     */
    public void removeIfExistsFor (RunningComServer runningComServer);

    /**
     * Finds or creates the {@link OutboundComPortMBean}
     * for the specified {@link ScheduledComPort outbound ComPort}.
     *
     * @param outboundComPort The ScheduledComPort
     * @return The OutboundComPortMBean
     */
    public OutboundComPortMBean findOrCreateFor (ScheduledComPort outboundComPort);

    /**
     * Finds or creates the {@link InboundComPortMBean}
     * for the specified {@link ComPortListener inbound ComPort}.
     *
     * @param inboundComPort The ComPortListener
     * @return The InboundComPortMBean
     */
    public InboundComPortMBean findOrCreateFor (ComPortListener inboundComPort);

}