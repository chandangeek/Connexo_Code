package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.inbound.InboundCommunicationHandler;
import com.energyict.mdc.engine.impl.web.EmbeddedWebServer;
import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.config.ServletBasedInboundComPort;

/**
 * Implementation of a ComPortListener which servers functionality to use a Jetty server to process
 * inbound servlet based protocols. My only responsibility is to keep the Jetty server up and
 * now and then check if we need to change our configuration.
 * <p/>
 * Copyrights EnergyICT
 * Date: 19/10/12
 * Time: 11:39
 */
public class ServletInboundComPortListener extends ServletBasedComPortListenerImpl {

    private final InboundCommunicationHandler.ServiceProvider serviceProvider;
    private EmbeddedWebServer embeddedWebServer;
    private long sleepTime;

    public ServletInboundComPortListener(InboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, InboundCommunicationHandler.ServiceProvider serviceProvider) {
        super(comPort, comServerDAO, deviceCommandExecutor);
        this.serviceProvider = serviceProvider;
        this.embeddedWebServer = serviceProvider.embeddedWebServerFactory().findOrCreateFor(getServletBasedInboundComPort(), comServerDAO, deviceCommandExecutor, serviceProvider);
        this.sleepTime = getComPort().getComServer().getChangesInterPollDelay().getMilliSeconds();
    }

    private ServletBasedInboundComPort getServletBasedInboundComPort() {
        return (ServletBasedInboundComPort) getComPort();
    }

    @Override
    protected void doStart() {
        super.doStart();
        this.embeddedWebServer.start();
    }

    @Override
    protected void doShutdown() {
        this.embeddedWebServer.shutdown();
        super.doShutdown();
    }

    @Override
    public void shutdownImmediate() {
        this.embeddedWebServer.shutdownImmediate();
        super.shutdownImmediate();
    }

    @Override
    protected void doRun() {
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    protected void applyChangesForNewComPort(InboundComPort inboundComPort) {
        this.embeddedWebServer.shutdown(); // is already a blocking call until the embedded webserver has shut down
        this.embeddedWebServer = this.serviceProvider.embeddedWebServerFactory().findOrCreateFor((ServletBasedInboundComPort) inboundComPort, getComServerDAO(), getDeviceCommandExecutor(), serviceProvider);
        if (inboundComPort.isActive()) {
            this.embeddedWebServer.start();
            this.sleepTime = inboundComPort.getComServer().getChangesInterPollDelay().getMilliSeconds();
        }
    }

}
