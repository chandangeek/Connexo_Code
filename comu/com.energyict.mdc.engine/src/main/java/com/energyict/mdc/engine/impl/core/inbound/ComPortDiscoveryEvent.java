package com.energyict.mdc.engine.impl.core.inbound;

import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.impl.events.logging.CommunicationLoggingEvent;
import com.energyict.mdc.engine.impl.logging.LogLevel;

/**
 * Copyrights EnergyICT
 * Date: 15/01/2016
 * Time: 15:03
 */
public class ComPortDiscoveryEvent extends CommunicationLoggingEvent {

    public ComPortDiscoveryEvent(ServiceProvider serviceProvider, ConnectionTask connectionTask, ComPort comPort, LogLevel logLevel, String logMessage) {
        super(serviceProvider, connectionTask, comPort, logLevel, logMessage);
    }

}
