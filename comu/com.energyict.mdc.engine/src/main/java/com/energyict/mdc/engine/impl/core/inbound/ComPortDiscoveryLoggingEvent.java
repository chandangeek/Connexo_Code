/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core.inbound;

import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.impl.events.logging.CommunicationLoggingEvent;
import com.energyict.mdc.engine.impl.logging.LogLevel;

class ComPortDiscoveryLoggingEvent extends CommunicationLoggingEvent {

    ComPortDiscoveryLoggingEvent(ServiceProvider serviceProvider, ConnectionTask connectionTask, ComPort comPort, LogLevel logLevel, String logMessage) {
        super(serviceProvider, connectionTask, comPort, logLevel, logMessage);
    }

}
