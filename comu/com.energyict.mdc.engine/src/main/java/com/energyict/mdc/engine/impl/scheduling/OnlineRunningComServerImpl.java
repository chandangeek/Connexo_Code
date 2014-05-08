package com.energyict.mdc.engine.impl.scheduling;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.UserService;
import com.energyict.cbo.PooledThreadFactory;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.ServerProcess;
import com.energyict.mdc.engine.impl.core.ServerProcessStatus;
import com.energyict.mdc.engine.impl.core.online.ComServerDAOImpl;
import com.energyict.comserver.scheduling.factories.ComPortListenerFactory;
import com.energyict.comserver.scheduling.factories.ScheduledComPortFactory;
import com.energyict.mdc.engine.impl.web.EmbeddedWebServer;
import com.energyict.mdc.engine.impl.web.EmbeddedWebServerFactory;
import com.energyict.mdc.ManagerFactory;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.RemoteComServer;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdw.task.SystemTopicHandler;
import com.energyict.mdw.task.SystemTopicHandlerException;

import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Extends the {@link RunningComServerImpl} and specializes on
 * {@link com.energyict.mdc.engine.model.OnlineComServer}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-08 (15:54)
 */
public class OnlineRunningComServerImpl extends RunningComServerImpl {

    private OnlineComServer comServer;
    private EmbeddedWebServer remoteQueryApi;
    private SystemTopicHandler systemTopicHandler;

    public OnlineRunningComServerImpl(OnlineComServer comServer, ThreadPrincipalService threadPrincipalService, UserService userService, IssueService issueService) {
        this(comServer, new ComServerDAOImpl(), threadPrincipalService, userService, issueService);
    }


    private OnlineRunningComServerImpl(OnlineComServer comServer, ComServerDAO comServerDAO, ThreadPrincipalService threadPrincipalService, UserService userService, IssueService issueService) {
        super(comServer, comServerDAO, null, null, new PooledThreadFactory(), new CleanupDuringStartupImpl(comServer, comServerDAO), threadPrincipalService, userService, issueService);
        this.comServer = comServer;
    }

    public OnlineRunningComServerImpl(OnlineComServer comServer, ComServerDAO comServerDAO, ScheduledComPortFactory scheduledComPortFactory, ComPortListenerFactory comPortListenerFactory, ThreadFactory threadFactory, CleanupDuringStartup cleanupDuringStartup, ThreadPrincipalService threadPrincipalService, UserService userService, IssueService issueService) {
        super(comServer, comServerDAO, scheduledComPortFactory, comPortListenerFactory, threadFactory, cleanupDuringStartup, threadPrincipalService, userService, issueService);
        this.comServer = comServer;
    }

    @Override
    protected void continueStartupAfterDAOStart () {
        this.startQueryApiListenerIfNecessary();
        this.startSystemTopicHandler();
        super.continueStartupAfterDAOStart();
    }

    private void startSystemTopicHandler() {
        this.systemTopicHandler = new SystemTopicHandler(getComServer().getName());
        Thread thread = getThreadFactory().newThread(this.systemTopicHandler);
        thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                Logger.getLogger(OnlineRunningComServerImpl.class.getName()).log(Level.SEVERE, "Uncaught exception", e);
                if (e instanceof SystemTopicHandlerException) {
                    System.exit(-10);
                }
            }
        });
        thread.start();
    }

    private void startQueryApiListenerIfNecessary () {
        List<RemoteComServer> remoteComServers = ManagerFactory.getCurrent().getComServerFactory().findRemoteComServersWithOnlineComServer(this.comServer);
        if (!remoteComServers.isEmpty()) {
            this.startQueryApiListener();
        }
    }

    private void startQueryApiListener () {
        this.remoteQueryApi = EmbeddedWebServerFactory.DEFAULT.get().findOrCreateRemoteQueryWebServer(this.comServer);
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
        this.systemTopicHandler.shutDownImmediate();
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

}