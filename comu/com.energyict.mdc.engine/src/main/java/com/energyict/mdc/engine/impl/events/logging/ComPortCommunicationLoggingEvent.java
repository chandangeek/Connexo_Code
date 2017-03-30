/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events.logging;

import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.impl.logging.LogLevel;

public class ComPortCommunicationLoggingEvent extends ComPortOperationsLoggingEvent {

    public ComPortCommunicationLoggingEvent(ServiceProvider serviceProvider, ComPort comPort, LogLevel logLevel, String logMessage) {
        super(serviceProvider, comPort, logLevel, logMessage);
    }

}
