package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.inbound.InboundCommunicationHandler;
import com.energyict.mdc.engine.impl.web.EmbeddedWebServer;

import java.time.Clock;
import java.util.concurrent.ThreadFactory;

/**
 * Models ComPortListener functionality specifically for a
 * {@link EmbeddedWebServer WebServer}
 * based {@link com.energyict.mdc.engine.config.ComPort ComPort}
 * <p>
 * Copyrights EnergyICT
 * Date: 12/11/12
 * Time: 9:12
 */
public abstract class ServletBasedComPortListenerImpl extends ComPortListenerImpl {

    protected ServletBasedComPortListenerImpl(RunningComServer runningComServer, InboundComPort comPort, Clock clock, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, InboundCommunicationHandler.ServiceProvider serviceProvider) {
        super(runningComServer, comPort, clock, comServerDAO, deviceCommandExecutor, serviceProvider);
    }

    protected ServletBasedComPortListenerImpl(RunningComServer runningComServer, InboundComPort comPort, Clock clock, ComServerDAO comServerDAO, ThreadFactory threadFactory, DeviceCommandExecutor deviceCommandExecutor, InboundCommunicationHandler.ServiceProvider serviceProvider) {
        super(runningComServer, comPort, clock, comServerDAO, threadFactory, deviceCommandExecutor, serviceProvider);
    }
}