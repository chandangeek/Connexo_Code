/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.users.User;
import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.config.ServletBasedInboundComPort;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.inbound.InboundCommunicationHandler;
import com.energyict.mdc.engine.impl.web.EmbeddedWebServer;

import java.util.Locale;

public class ServletInboundComPortListener extends ServletBasedComPortListenerImpl {

    private EmbeddedWebServer embeddedWebServer;
    private long sleepTime;

    public ServletInboundComPortListener(RunningComServer runningComServer, InboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, InboundCommunicationHandler.ServiceProvider serviceProvider) {
        super(runningComServer, comPort, serviceProvider.clock(), comServerDAO, deviceCommandExecutor, serviceProvider);
        this.embeddedWebServer = serviceProvider.embeddedWebServerFactory().findOrCreateFor(getServletBasedInboundComPort(), comServerDAO, deviceCommandExecutor, serviceProvider);
        this.sleepTime = getComPort().getComServer().getChangesInterPollDelay().getMilliSeconds();
    }

    private ServletBasedInboundComPort getServletBasedInboundComPort() {
        return (ServletBasedInboundComPort) getComPort();
    }

    @Override
    public int getThreadCount() {
        return 1;
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
            this.registerActivity();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    protected void setThreadPrinciple() {
        User comServerUser = getComServerDAO().getComServerUser();
        getServiceProvider().threadPrincipalService().set(comServerUser, "ServletInboundComPortListener", "Executing", comServerUser.getLocale().orElse(Locale.ENGLISH));
    }

}
