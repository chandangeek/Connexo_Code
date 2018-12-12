/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.monitor;

import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.impl.core.ComPortListener;
import com.energyict.mdc.engine.impl.core.RunningComServer;
import com.energyict.mdc.engine.impl.core.ScheduledComPort;

import java.util.Optional;

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
     ComServerMonitorImplMBean findOrCreateFor (RunningComServer runningComServer);

    /**
     * Notifies this ManagementBeanFactory that the name
     * of the specified RunningComServer was changed.
     *
     * @param oldName The old name of the specified RunningComServer
     * @param runningComServer The RunningComServer
     */
     void renamed (String oldName, RunningComServer runningComServer);

    /**
     * Finds the {@link ComServerMonitorImplMBean} for the specified {@link ComServer}.
     *
     * @param comServer The ComServer
     * @return The ComServerMonitorImplMBean or <code>null</code> if the ComServer has not registered yet
     */
     Optional<ComServerMonitorImplMBean> findFor(ComServer comServer);

    /**
     * Removes the {@link ComServerMonitorImplMBean}
     * for the specified {@link RunningComServer}
     * if it already exists and does nothing when
     * the ComServerMonitorImplMBean does not exist.
     *
     * @param runningComServer The RunningComServer
     */
     void removeIfExistsFor (RunningComServer runningComServer);

    /**
     * Finds or creates the {@link ScheduledComPortMBean}
     * for the specified {@link ScheduledComPort outbound ComPort}.
     *
     * @param comPort The ScheduledComPort
     * @return The OutboundComPortMBean
     */
     ScheduledComPortMonitorImplMBean findOrCreateFor (ScheduledComPort comPort);

    /**
     * Finds or creates the {@link ScheduledComPortMBean}
     * for the specified {@link ScheduledComPort outbound ComPort}.
     *
     * @param comPort The ScheduledComPort
     * @return The OutboundComPortMBean
     */
    public Optional<ScheduledComPortMonitorImplMBean> findFor (OutboundComPort comPort);

    /**
     * Removes the {@link ScheduledComPortMBean}
     * for the specified {@link ScheduledComPort}
     * if it already exists and does nothing when
     * the OutboundComPortImplMBean does not exist.
     *
     * @param comPort The RunningComServer
     */
    public void removeIfExistsFor (ScheduledComPort comPort);

    /**
     * Finds or creates the {@link ScheduledComPortMBean}
     * for the specified {@link ScheduledComPort outbound ComPort}.
     *
     * @param inboundComPort The ComPortListener
     * @return The InboundComPortMBean
     */
     InboundComPortMonitorImplMBean findOrCreateFor (ComPortListener inboundComPort);

    /**
     * Finds or creates the {@link InboundComPortMBean}
     * for the specified {@link InboundComPort inbound ComPort}.
     *
     * @param inboundComPort The ComPortListener
     * @return The InboundComPortMBean
     */
     Optional<InboundComPortMonitorImplMBean> findFor (InboundComPort inboundComPort);

    /**
     * Removes the {@link InboundComPortMBean}
     * for the specified {@link ComPortListener}
     * if it already exists and does nothing when
     * the OutboundComPortImplMBean does not exist.
     *
     * @param inboundComPort The ComPortListener
     */
     void removeIfExistsFor (ComPortListener inboundComPort);

}