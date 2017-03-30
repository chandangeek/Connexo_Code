/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.device.data.tasks.OutboundConnectionTask;
import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.impl.logging.Configuration;
import com.energyict.mdc.engine.impl.logging.LogLevel;
/**
 * Defines all the log messages for {@link DeviceCommand}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-08-23 (15:09)
 */
public interface DeviceCommandLogger {

    /**
     * Logs that the creation of an outbound ComSession
     * failed because of some business or database constraint reported by
     * ComServerDAO.
     *
     * @param e The details of the business constraint that was violated
     * @param connectionTask The OutB
     */
    @Configuration(format = "The creation of an outbound communication session for task ''{0}'' failed, see attached stacktrace", logLevel = LogLevel.ERROR)
    public void outboundComSessionCreationFailed (RuntimeException e, OutboundConnectionTask connectionTask);

    /**
     * Logs that the creation of an inbound ComSession
     * failed because of some business or database constraint reported by
     * ComServerDAO.
     *
     * @param e The details of the business constraint that was violated
     * @param comPort The InboundComPort
     */
    @Configuration(format = "The creation of an inbound communication session for port ''{0}'' failed, see attached stacktrace", logLevel = LogLevel.ERROR)
    public void inboundComSessionCreationFailed (RuntimeException e, InboundComPort comPort);

    /**
     * Logs that the creation of an inbound ComSession
     * failed because of some business or database constraint reported by
     * ComServerDAO.
     *
     * @param e The details of the business constraint that was violated
     * @param comPort The InboundComPort
     * @param connectionTask The InboundConnectionTask
     */
    @Configuration(format = "The creation of an inbound communication session for port ''{0}'' and task ''{1}'' failed, see attached stacktrace", logLevel = LogLevel.ERROR)
    public void inboundComSessionCreationFailed (RuntimeException e, InboundComPort comPort, InboundConnectionTask connectionTask);

}