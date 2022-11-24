/*
 * Copyright (c) 2022 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.users.User;
import com.energyict.mdc.common.comserver.CoapBasedInboundComPort;
import com.energyict.mdc.common.comserver.InboundComPort;
import com.energyict.mdc.engine.impl.coap.EmbeddedCoapServer;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.inbound.InboundCommunicationHandler;

import java.util.Locale;

public class CoapInboundComPortListener extends CoapBasedComPortListenerImpl {

    private final EmbeddedCoapServer embeddedCoapServer;
    private final long sleepTime;

    public CoapInboundComPortListener(RunningComServer runningComServer, InboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, InboundCommunicationHandler.ServiceProvider serviceProvider) {
        super(runningComServer, comPort, serviceProvider.clock(), comServerDAO, deviceCommandExecutor, serviceProvider);
        this.embeddedCoapServer = serviceProvider.embeddedCoapServerFactory().findOrCreateFor(getCoapBasedInboundComPort(), comServerDAO, deviceCommandExecutor, serviceProvider);
        this.sleepTime = getComPort().getComServer().getChangesInterPollDelay().getMilliSeconds();
    }

    private CoapBasedInboundComPort getCoapBasedInboundComPort() {
        return (CoapBasedInboundComPort) getComPort();
    }

    @Override
    public int getThreadCount() {
        return 1;
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
        getServiceProvider().threadPrincipalService().set(comServerUser, "CoapInboundComPortListener", "Executing", comServerUser.getLocale().orElse(Locale.ENGLISH));
    }

    @Override
    protected void doStart() {
        super.doStart();
        this.embeddedCoapServer.start();
    }

    @Override
    public void shutdownImmediate() {
        this.embeddedCoapServer.shutdownImmediate();
        super.shutdownImmediate();
    }

    @Override
    protected void doShutdown() {
        this.embeddedCoapServer.shutdown();
        super.doShutdown();
    }
}
