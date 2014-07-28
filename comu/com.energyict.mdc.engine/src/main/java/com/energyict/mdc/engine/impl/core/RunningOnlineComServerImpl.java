package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.engine.impl.core.factories.ComPortListenerFactory;
import com.energyict.mdc.engine.impl.core.factories.ScheduledComPortFactory;
import com.energyict.mdc.engine.impl.core.online.ComServerDAOImpl;
import com.energyict.mdc.engine.impl.web.EmbeddedWebServer;
import com.energyict.mdc.engine.impl.web.queryapi.WebSocketQueryApiService;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.RemoteComServer;

import java.util.List;
import java.util.concurrent.ThreadFactory;

/**
 * Extends the {@link RunningComServerImpl} and specializes on
 * {@link com.energyict.mdc.engine.model.OnlineComServer}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-08 (15:54)
 */
public class RunningOnlineComServerImpl extends RunningComServerImpl implements RunningOnlineComServer {

    private OnlineComServer comServer;
    private EmbeddedWebServer remoteQueryApi;

    public RunningOnlineComServerImpl(OnlineComServer comServer, ServiceProvider serviceProvider) {
        this(comServer, new ComServerDAOImpl(serviceProvider), serviceProvider);
    }

    private RunningOnlineComServerImpl(OnlineComServer comServer, ComServerDAO comServerDAO, ServiceProvider serviceProvider) {
        super(comServer, comServerDAO, null, null, new ComServerThreadFactory(comServer), new CleanupDuringStartupImpl(comServer, comServerDAO), serviceProvider);
        this.comServer = comServer;
    }

    public RunningOnlineComServerImpl(OnlineComServer comServer, ComServerDAO comServerDAO, ScheduledComPortFactory scheduledComPortFactory, ComPortListenerFactory comPortListenerFactory, ThreadFactory threadFactory, CleanupDuringStartup cleanupDuringStartup, ServiceProvider serviceProvider) {
        super(comServer, comServerDAO, scheduledComPortFactory, comPortListenerFactory, threadFactory, cleanupDuringStartup, serviceProvider);
        this.comServer = comServer;
    }

    @Override
    public OnlineComServer getComServer() {
        return this.comServer;
    }

    @Override
    protected void continueStartupAfterDAOStart () {
        this.startQueryApiListenerIfNecessary();
        super.continueStartupAfterDAOStart();
    }

    private void startQueryApiListenerIfNecessary () {
        List<RemoteComServer> remoteComServers = getServiceProvider().engineModelService().findRemoteComServersForOnlineComServer(this.comServer);
        if (!remoteComServers.isEmpty()) {
            this.startQueryApiListener();
        }
    }

    private void startQueryApiListener () {
        this.remoteQueryApi = this.getServiceProvider().embeddedWebServerFactory().findOrCreateRemoteQueryWebServer(this);
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
        return this.getServiceProvider().webSocketQueryApiServiceFactory().newWebSocketQueryApiService(this);
    }

    @Override
    public void queryApiClientRegistered () {
        this.getOperationalMonitor().getQueryApiStatistics().clientRegistered();
    }

    @Override
    public void queryApiClientUnregistered () {
        this.getOperationalMonitor().getQueryApiStatistics().clientUnregistered();
    }

    @Override
    public void queryApiCallCompleted(long executionTimeInMillis) {
        this.getOperationalMonitor().getQueryApiStatistics().callCompleted(executionTimeInMillis);
    }

    @Override
    public void queryApiCallFailed(long executionTimeInMillis) {
        this.getOperationalMonitor().getQueryApiStatistics().callFailed(executionTimeInMillis);
    }

}