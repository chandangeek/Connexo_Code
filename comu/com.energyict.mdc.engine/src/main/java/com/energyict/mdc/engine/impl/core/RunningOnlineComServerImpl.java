/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.engine.config.OnlineComServer;
import com.energyict.mdc.engine.config.RemoteComServer;
import com.energyict.mdc.engine.impl.core.factories.ComPortListenerFactory;
import com.energyict.mdc.engine.impl.core.factories.ScheduledComPortFactory;
import com.energyict.mdc.engine.impl.monitor.ServerQueryAPIStatistics;
import com.energyict.mdc.engine.impl.web.EmbeddedWebServer;
import com.energyict.mdc.engine.impl.web.EmbeddedWebServerFactory;
import com.energyict.mdc.engine.impl.web.queryapi.WebSocketQueryApiService;

import java.util.List;
import java.util.concurrent.ThreadFactory;

/**
 * Extends the {@link RunningComServerImpl} and specializes on
 * {@link com.energyict.mdc.engine.config.OnlineComServer}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-08 (15:54)
 */
public class RunningOnlineComServerImpl extends RunningComServerImpl implements RunningOnlineComServer {

    private EmbeddedWebServer remoteQueryApi;

    public RunningOnlineComServerImpl(OnlineComServer comServer, ComServerDAO comServerDAO, ServiceProvider serviceProvider) {
        super(comServer, comServerDAO, null, null, new ComServerThreadFactory(comServer), serviceProvider);
    }

    public RunningOnlineComServerImpl(OnlineComServer comServer, ComServerDAO comServerDAO, ScheduledComPortFactory scheduledComPortFactory, ComPortListenerFactory comPortListenerFactory, ThreadFactory threadFactory, ServiceProvider serviceProvider) {
        super(comServer, comServerDAO, scheduledComPortFactory, comPortListenerFactory, threadFactory, serviceProvider);
    }

    public RunningOnlineComServerImpl(OnlineComServer comServer, ComServerDAO comServerDAO, ScheduledComPortFactory scheduledComPortFactory, ComPortListenerFactory comPortListenerFactory, ThreadFactory threadFactory, EmbeddedWebServerFactory embeddedWebServerFactory, ServiceProvider serviceProvider) {
        super(comServer, comServerDAO, scheduledComPortFactory, comPortListenerFactory, threadFactory, embeddedWebServerFactory, serviceProvider);
    }

    @Override
    public OnlineComServer getComServer() {
        return (OnlineComServer) super.getComServer();
    }

    @Override
    protected void continueStartupAfterDAOStart () {
        this.startQueryApiListenerIfNecessary();
        super.continueStartupAfterDAOStart();
    }

    private void startQueryApiListenerIfNecessary () {
        List<RemoteComServer> remoteComServers = getServiceProvider().engineConfigurationService().findRemoteComServersForOnlineComServer(getComServer());
        if (!remoteComServers.isEmpty()) {
            this.startQueryApiListener();
        }
    }

    private void startQueryApiListener () {
        this.remoteQueryApi = this.getEmbeddedWebServerFactory().findOrCreateRemoteQueryWebServer(this);
        this.remoteQueryApi.start();
    }

    @Override
    protected void shutdownNestedServerProcesses (boolean immediate) {
        super.shutdownNestedServerProcesses(immediate);
        if (this.isRemoteQueryApiStarted()) {
            if (immediate) {
                this.remoteQueryApi.shutdownImmediate();
            }
            else {
                this.remoteQueryApi.shutdown();
            }
        }
    }

    @Override
    protected List<ServerProcess> getNestedServerProcesses () {
        List<ServerProcess> nestedServerProcesses = super.getNestedServerProcesses();
        if (this.isRemoteQueryApiStarted()) {
            nestedServerProcesses.add(this.remoteQueryApi);
        }
        return nestedServerProcesses;
    }

    @Override
    public boolean isRemoteQueryApiStarted () {
        return this.remoteQueryApi != null && ServerProcessStatus.STARTED.equals(this.remoteQueryApi.getStatus());
    }

    @Override
    public WebSocketQueryApiService newWebSocketQueryApiService() {
        return new WebSocketQueryApiService(
                this,
                this.getComServerDAO(),
                this.getServiceProvider().engineConfigurationService(),
                this.getServiceProvider().connectionTaskService(),
                this.getServiceProvider().communicationTaskService(),
                this.getServiceProvider().transactionService());
    }

    @Override
    public void queryApiClientRegistered () {
        ((ServerQueryAPIStatistics) this.getOperationalMonitor().getQueryApiStatistics()).clientRegistered();
    }

    @Override
    public void queryApiClientUnregistered () {
        ((ServerQueryAPIStatistics) this.getOperationalMonitor().getQueryApiStatistics()).clientUnregistered();
    }

    @Override
    public void queryApiCallCompleted(long executionTimeInMillis) {
        ((ServerQueryAPIStatistics) this.getOperationalMonitor().getQueryApiStatistics()).callCompleted(executionTimeInMillis);
    }

    @Override
    public void queryApiCallFailed(long executionTimeInMillis) {
        ((ServerQueryAPIStatistics) this.getOperationalMonitor().getQueryApiStatistics()).callFailed(executionTimeInMillis);
    }

}