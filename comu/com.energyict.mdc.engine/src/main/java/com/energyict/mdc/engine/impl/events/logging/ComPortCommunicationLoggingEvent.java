package com.energyict.mdc.engine.impl.events.logging;

import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.impl.logging.LogLevel;

/**
 * ComPort Operations Event that should be logged as a {@Link CommunicationLoggingEvent}
 * Copyrights EnergyICT
 * Date: 18/01/2016
 * Time: 12:51
 */
public class ComPortCommunicationLoggingEvent extends ComPortOperationsLoggingEvent {

    public ComPortCommunicationLoggingEvent(ServiceProvider serviceProvider, ComPort comPort, LogLevel logLevel, String logMessage) {
        super(serviceProvider, comPort, logLevel, logMessage);
    }

}
