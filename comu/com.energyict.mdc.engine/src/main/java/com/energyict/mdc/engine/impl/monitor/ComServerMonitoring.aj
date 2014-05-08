package com.energyict.mdc.engine.impl.monitor;

import com.energyict.mdc.engine.impl.core.ServerProcessStatus;
import com.energyict.mdc.engine.impl.core.remote.QueryMethod;
import com.energyict.mdc.engine.exceptions.DataAccessException;
import com.energyict.mdc.engine.impl.events.EventPublisherImpl;
import com.energyict.comserver.scheduling.RunningComServer;
import com.energyict.comserver.scheduling.RunningComServerImpl;
import com.energyict.comserver.web.queryapi.QueryApiServlet;
import com.energyict.comserver.web.queryapi.WebSocketQueryApiService;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides pointcuts and advice around {@link RunningComServer}
 * for monitoring purposes. The bulk of the work is done
 * by {@link ComServerMonitorImpl}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-03 (14:16)
 */
public aspect ComServerMonitoring {
    declare precedence:
            ComServerMonitoring,
            com.energyict.comserver.scheduling.aspects.logging.ComServerLogging;

    private Map<EventPublisherImpl, RunningComServer> eventPublisherRunningComServerMap = new HashMap<>();

    private pointcut starting (RunningComServer comServer):
           execution(void RunningComServer.start())
        && target(comServer);

    after (RunningComServer comServer): starting(comServer) {
        if (ServerProcessStatus.STARTED.equals(comServer.getStatus())) {
            ManagementBeanFactoryImpl.getInstance().findOrCreateFor(comServer);
        }
    }

    private pointcut shuttingDown (RunningComServer comServer):
           execution(void RunningComServer.shutdown())
        && target(comServer);

    before (RunningComServer comServer): shuttingDown(comServer) {
        ManagementBeanFactoryImpl.getInstance().removeIfExistsFor(comServer);
    }

    private pointcut shuttingDownImmediately (RunningComServer comServer):
           execution(void RunningComServer.shutdownImmediate())
        && target(comServer);

    before (RunningComServer comServer): shuttingDownImmediately(comServer) {
        ManagementBeanFactoryImpl.getInstance().removeIfExistsFor(comServer);
    }

    private pointcut startEventMechanism (RunningComServerImpl comServer):
           execution(void startEventMechanism())
        && target(comServer);

    after (RunningComServerImpl comServer): startEventMechanism(comServer) {
        this.eventPublisherRunningComServerMap.put(EventPublisherImpl.getInstance(), comServer);
    }

    private pointcut eventClientRegisters (EventPublisherImpl eventPublisher):
           execution(private * createFilter (*))
        && target(eventPublisher);

    after (EventPublisherImpl eventPublisher): eventClientRegisters(eventPublisher) {
        RunningComServer comServer = this.eventPublisherRunningComServerMap.get(eventPublisher);
        if (comServer != null) {    // May occur in test environment where the creation of filters is not done from an actual RunningComServer
            ComServerMonitor monitor = (ComServerMonitor) ManagementBeanFactoryImpl.getInstance().findOrCreateFor(comServer);
            monitor.getEventApiStatistics().clientRegistered();
        }
    }

    private pointcut eventClientUnregisters (EventPublisherImpl eventPublisher):
           execution(void unregisterAllInterests (*))
        && target(eventPublisher);

    before (EventPublisherImpl eventPublisher): eventClientUnregisters(eventPublisher) {
        RunningComServer comServer = this.eventPublisherRunningComServerMap.get(eventPublisher);
        if (comServer != null) {    // May occur in test environment where the creation of filters is not done from an actual RunningComServer
            ComServerMonitor monitor = (ComServerMonitor) ManagementBeanFactoryImpl.getInstance().findOrCreateFor(comServer);
            monitor.getEventApiStatistics().clientUnregistered();
        }
    }

    private pointcut publishEvent (EventPublisherImpl eventPublisher):
           execution(void publish (com.energyict.mdc.engine.events.ComServerEvent))
        && target(eventPublisher);

    before (EventPublisherImpl eventPublisher): publishEvent(eventPublisher) {
        RunningComServer comServer = this.eventPublisherRunningComServerMap.get(eventPublisher);
        if (comServer != null) {    // May occur in test environment where the creation of filters is not done from an actual RunningComServer
            ComServerMonitor monitor = (ComServerMonitor) ManagementBeanFactoryImpl.getInstance().findOrCreateFor(comServer);
            monitor.getEventApiStatistics().eventWasPublished();
        }
    }

    private pointcut queryClientRegisters (QueryApiServlet queryApiServlet):
            execution(private WebSocketQueryApiService createQueryApiService ())
         && target(queryApiServlet);

    after (QueryApiServlet queryApiServlet): queryClientRegisters(queryApiServlet) {
        ComServerMonitor monitor = (ComServerMonitor) ManagementBeanFactoryImpl.getInstance().findOrCreateFor(queryApiServlet.getComServer());
        if (monitor != null) {  // May occur in test conditions where the createQueryApiService method is not invoked from a RunningComServer context
            monitor.getQueryApiStatistics().clientRegistered();
        }
    }

    private pointcut queryClientUnregisters (WebSocketQueryApiService webSocketQueryApiService):
            execution(public void onClose (int, java.lang.String))
         && target(webSocketQueryApiService);

    after (WebSocketQueryApiService webSocketQueryApiService): queryClientUnregisters(webSocketQueryApiService) {
        ComServerMonitor monitor = (ComServerMonitor) ManagementBeanFactoryImpl.getInstance().findOrCreateFor(webSocketQueryApiService.getComServer());
        if (monitor != null) {  // May occur in test conditions where the onClose method is not invoked from a RunningComServer context
            monitor.getQueryApiStatistics().clientUnregistered();
        }
    }

    private pointcut remoteQueryExecutes (WebSocketQueryApiService webSocketQueryApiService, QueryMethod queryMethod, JSONObject jsonQuery):
            execution(private java.lang.String execute(QueryMethod, JSONObject))
         && target(webSocketQueryApiService)
         && args(queryMethod, jsonQuery);

    String around (WebSocketQueryApiService webSocketQueryApiService, QueryMethod queryMethod, JSONObject jsonQuery)
            throws JSONException, IOException:
            remoteQueryExecutes(webSocketQueryApiService, queryMethod, jsonQuery) {
        ComServerMonitor monitor = (ComServerMonitor) ManagementBeanFactoryImpl.getInstance().findOrCreateFor(webSocketQueryApiService.getComServer());
        long startTime = System.currentTimeMillis();
        try {
            String result = proceed(webSocketQueryApiService, queryMethod, jsonQuery);
            if (monitor != null) {  // May occur in test conditions where the execute method is not invoked from a RunningComServer context
                monitor.getQueryApiStatistics().callCompleted(System.currentTimeMillis() - startTime);
            }
            return result;
        }
        catch (JSONException | IOException | DataAccessException e) {
            if (monitor != null) {  // May occur in test conditions where the execute method is not invoked from a RunningComServer context
                monitor.getQueryApiStatistics().callFailed(System.currentTimeMillis() - startTime);
            }
            throw e;
        }
    }

}