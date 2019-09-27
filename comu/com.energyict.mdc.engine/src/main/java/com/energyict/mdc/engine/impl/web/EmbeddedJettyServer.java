/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.web;

import com.elster.jupiter.properties.BasicPropertySpec;
import com.elster.jupiter.properties.LongFactory;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.comserver.OnlineComServer;
import com.energyict.mdc.common.comserver.ServletBasedInboundComPort;
import com.energyict.mdc.common.pluggable.PluggableClass;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.RunningOnlineComServer;
import com.energyict.mdc.engine.impl.core.ServerProcessStatus;
import com.energyict.mdc.engine.impl.core.inbound.InboundCommunicationHandler;
import com.energyict.mdc.engine.impl.web.events.EventServlet;
import com.energyict.mdc.engine.impl.web.events.WebSocketEventPublisherFactory;
import com.energyict.mdc.engine.impl.web.queryapi.QueryApiServlet;
import com.energyict.mdc.engine.monitor.EventAPIStatistics;
import com.energyict.mdc.engine.monitor.QueryAPIStatistics;
import com.energyict.mdc.upl.TypedProperties;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.joda.time.DateTimeConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.elster.jupiter.util.Checks.is;

/**
 * Provides an implementation for the {@link EmbeddedWebServer} interface
 * for the <a href="http://www.eclipse.org/jetty/">Jetty</a> web server.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-11 (15:43)
 */
public class EmbeddedJettyServer implements EmbeddedWebServer {

    private static final Logger LOGGER = Logger.getLogger( EmbeddedJettyServer.class.getName() );

    private final ShutdownFailureLogger shutdownFailureLogger;

    public interface ServiceProvider {

        public WebSocketEventPublisherFactory webSocketEventPublisherFactory();

    }

    private static final int GRACEFUL_SHUTDOWN_SECONDS = 3;
    private Server jetty;
    private String threadPoolName;

    private EmbeddedJettyServer (ShutdownFailureLogger shutdownFailureLogger) {
        super();
        this.shutdownFailureLogger = shutdownFailureLogger;
    }

    /**
     * The number of seconds that accepted requests are allowed to complete
     * during the graceful shutdown.
     */

    private final static String INBOUND_COMPORT_SERVICE = "Jetty_InboundComportService";

    /**
     * Constructs a new EmbeddedJettyServer that will host
     * the servlet that supports inbound communication
     * on the specified {@link ServletBasedInboundComPort ComPort}.
     * It will use the {@link ComServerDAO} to get access to persistent data.
     * Furthermore, it will use the {@link DeviceCommandExecutor} to execute
     * commands against devices for which data was collected.
     * @param comPort The ServerServletBasedInboundComPort
     * @param comServerDAO The ComServerDAO
     * @param deviceCommandExecutor The DeviceCommandExecutor
     * @param serviceProvider The IssueService
     */
    public static EmbeddedJettyServer newForInboundDeviceCommunication(ServletBasedInboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, InboundCommunicationHandler.ServiceProvider serviceProvider){
        return new EmbeddedJettyServer(comPort, comServerDAO, deviceCommandExecutor, serviceProvider);
    }

    private EmbeddedJettyServer (ServletBasedInboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, InboundCommunicationHandler.ServiceProvider serviceProvider) {
        super();
        threadPoolName = INBOUND_COMPORT_SERVICE;
        QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setName(threadPoolName);
        jetty = new Server(threadPool);
        this.shutdownFailureLogger = new ComPortShutdownFailureLogger(comPort);
        ServerConnector connector;
        if (comPort.isHttps()) {
            SslContextFactory sslContextFactory = new SslContextFactory();
            sslContextFactory.setKeyStorePath(comPort.getKeyStoreSpecsFilePath());
            sslContextFactory.setKeyStorePassword(comPort.getKeyStoreSpecsPassword());
            sslContextFactory.setTrustStorePath(comPort.getTrustStoreSpecsFilePath());
            sslContextFactory.setTrustStorePassword(comPort.getTrustStoreSpecsPassword());
            connector = new ServerConnector(jetty, sslContextFactory);
        }
        else {
            connector = new ServerConnector(jetty);
        }
        connector.setPort(comPort.getPortNumber());
        connector.setIdleTimeout(getMaxIdleTime(comPort));
        jetty.addConnector(connector);

        initCustomErrorHandling();
        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        ComServlet servlet = new ComServlet(comPort, comServerDAO, deviceCommandExecutor, serviceProvider);
        handler.addServlet(new ServletHolder(servlet), this.getContextPath(comPort));
        this.jetty.setHandler(handler);
    }

    private static final String MAX_IDLE_TIME = "maxIdleTime";
    private static final BigDecimal MAX_IDLE_TIME_DEFAULT_VALUE = BigDecimal.valueOf(200000);

    /**
     * Get the maxIdleTime property that is defined on the discovery protocol of the ComPort pool.
     */
    private int getMaxIdleTime(ServletBasedInboundComPort comPort) {
        BasicPropertySpec idleTime = new BasicPropertySpec(new LongFactory());
        PluggableClass discoveryProtocolPluggableClass = comPort.getComPortPool().getDiscoveryProtocolPluggableClass();
        TypedProperties properties = discoveryProtocolPluggableClass.getProperties(Arrays.asList(idleTime));
        return properties.getTypedProperty(MAX_IDLE_TIME, MAX_IDLE_TIME_DEFAULT_VALUE).intValue();
    }

    private void initCustomErrorHandling() {
        ErrorHandler errorHandler = new ErrorHandler() {
            @Override
            protected void writeErrorPageBody(HttpServletRequest request, Writer writer, int code, String message, boolean showStacks) throws IOException {
                String uri = request.getRequestURI();
                writeErrorPageMessage(request, writer, code, message, uri);
            }

            @Override
            protected void writeErrorPageMessage(HttpServletRequest request, Writer writer, int code, String message, String uri) throws IOException {
                writer.write("<h2>HTTP ERROR ");
                writer.write(Integer.toString(code));
                writer.write("</h2>");
            }
        };
        errorHandler.setShowStacks(false);
        jetty.addBean(errorHandler);
    }

    private String getContextPath (ServletBasedInboundComPort comPort) {
        String contextPath = comPort.getContextPath();
        if (is(contextPath).empty()) {
            return "/";
        }
        else  if (!contextPath.startsWith("/")) {
            return  "/" + contextPath;
        }
        else {
            return contextPath;
        }
    }

    private final static String EVENT_MECHANISM = "Jetty_EventMechanism";

    /**
     * Creates a new EmbeddedJettyServer that will host the servlet for the event mechanism.
     *
     * @param eventRegistrationUri The URI on which the servlet should be listening
     * @param eventAPIStatistics The EventAPIStatistics
     */
    public static EmbeddedJettyServer newForEventMechanism (URI eventRegistrationUri, ServiceProvider serviceProvider, EventAPIStatistics eventAPIStatistics) {
        EmbeddedJettyServer server = new EmbeddedJettyServer(new EventMechanismShutdownFailureLogger(eventRegistrationUri));
        server.addEventMechanism(eventRegistrationUri, serviceProvider, eventAPIStatistics);
        return server;
    }

    public void addEventMechanism(URI eventRegistrationUri, ServiceProvider serviceProvider, EventAPIStatistics eventAPIStatistics) {
        this.jetty = new Server(getPortNumber(eventRegistrationUri, ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER));
        ContextHandlerCollection handlerCollection = new ContextHandlerCollection();
        this.jetty.setHandler(handlerCollection);
        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        ServletHolder servletHolder = new ServletHolder(new EventServlet(serviceProvider.webSocketEventPublisherFactory(), eventAPIStatistics));
        handler.addServlet(servletHolder, eventRegistrationUri.getPath());
        handlerCollection.setHandlers(new Handler[]{handler});
    }

    /**
     * Creates a new EmbeddedJettyServer that will host
     * the servlet for the remote query api of the specified
     * {@link OnlineComServer online comserver}.
     *
     * @param queryApiPostUri The URI on which the servlet should be listening
     * @param comServer The OnlineComServer
     */
    public static EmbeddedJettyServer newForQueryApi (URI queryApiPostUri, RunningOnlineComServer comServer, QueryAPIStatistics queryAPIStatistics) {
        EmbeddedJettyServer server = new EmbeddedJettyServer(new QueryAPIShutdownFailureLogger(queryApiPostUri));
        server.addQueryApi(queryApiPostUri, comServer, queryAPIStatistics);
        return server;
    }

    public void addQueryApi (URI queryApiPostUri, RunningOnlineComServer comServer, QueryAPIStatistics queryAPIStatistics) {
        this.jetty = new Server(getPortNumber(queryApiPostUri, ComServer.DEFAULT_QUERY_API_PORT_NUMBER));
        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        ServletHolder servletHolder = new ServletHolder(new QueryApiServlet(comServer, queryAPIStatistics));
        handler.addServlet(servletHolder, queryApiPostUri.getPath());
        this.jetty.setHandler(handler);
    }

    private static int getPortNumber (URI uri, int defaultPort) {
        int port = uri.getPort();
        if (port == -1) {
            return defaultPort;
        }
        else {
            return port;
        }
    }

    @Override
    public ServerProcessStatus getStatus () {
        if (this.jetty == null) {
            return ServerProcessStatus.STARTING;
        }
        else {
            return this.convertJettyStateToServerProcessStatus();
        }
    }

    private ServerProcessStatus convertJettyStateToServerProcessStatus () {
        if (this.jetty.isStarting()) {
            return ServerProcessStatus.STARTING;
        }
        else if (this.jetty.isStarted()) {
            return ServerProcessStatus.STARTED;
        }
        else if (this.jetty.isStopping()) {
            return ServerProcessStatus.SHUTTINGDOWN;
        }
        else if (this.jetty.isStopped()) {
            return ServerProcessStatus.SHUTDOWN;
        }
        else {
            return ServerProcessStatus.STARTING;
        }
    }

    @Override
    public void start () {
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
        try {
            jetty.setStopAtShutdown(true);
            this.jetty.start();
        } catch (Exception e) {
            e.printStackTrace(System.err);
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } finally {
            Thread.currentThread().setContextClassLoader(tccl);
        }
    }

    @Override
    public void shutdown () {
        this.shutdown(false);
    }

    @Override
    public void shutdownImmediate () {
        this.shutdown(true);
    }

    private void shutdown (boolean immediate) {
        try {
            if (immediate) {
                this.jetty.setStopTimeout(0);
                this.jetty.setStopAtShutdown(false);
            }
            else {
                this.jetty.setStopTimeout(DateTimeConstants.MILLIS_PER_SECOND * GRACEFUL_SHUTDOWN_SECONDS);
                this.jetty.setStopAtShutdown(true);
            }
            this.jetty.stop();
        }
        catch (Exception e) {
            this.shutdownFailureLogger.log(e, Logger.getLogger(EmbeddedJettyServer.class.getName()));
        }
    }

    private interface ShutdownFailureLogger {
        void log(Exception e, Logger logger);
    }

    private static class ComPortShutdownFailureLogger implements ShutdownFailureLogger {
        private final ServletBasedInboundComPort comPort;


        private ComPortShutdownFailureLogger(ServletBasedInboundComPort comPort) {
            super();
            this.comPort = comPort;
        }

        @Override
        public void log(Exception e, Logger logger) {
            String message = "Embedded jetty server for communication port " + this.comPort.getName() + "(" + this.comPort.getComServer().getName() + ") failed to stop";
            logger.info(message);
            logger.log(Level.FINE, message, e);
        }
    }

    private static class EventMechanismShutdownFailureLogger implements ShutdownFailureLogger {
        private final URI eventRegistrationUri;

        private EventMechanismShutdownFailureLogger(URI eventRegistrationUri) {
            super();
            this.eventRegistrationUri = eventRegistrationUri;
        }

        @Override
        public void log(Exception e, Logger logger) {
            String message = "Embedded jetty server for the event mechanism (" + this.eventRegistrationUri.toASCIIString() + ") failed to stop";
            logger.info(message);
            logger.log(Level.FINE, message, e);
        }
    }

    private static class QueryAPIShutdownFailureLogger implements ShutdownFailureLogger {
        private final URI queryApiPostUri;

        private QueryAPIShutdownFailureLogger(URI queryApiPostUri) {
            super();
            this.queryApiPostUri = queryApiPostUri;
        }

        @Override
        public void log(Exception e, Logger logger) {
            String message = "Embedded jetty server for the query api (" + this.queryApiPostUri.toASCIIString() +") failed to stop";
            logger.info(message);
            logger.log(Level.FINE, message, e);
        }
    }
}