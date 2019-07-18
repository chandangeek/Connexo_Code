/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.web;

import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.OnlineComServer;
import com.energyict.mdc.engine.config.ServletBasedInboundComPort;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.RunningOnlineComServer;
import com.energyict.mdc.engine.impl.core.ServerProcessStatus;
import com.energyict.mdc.engine.impl.core.inbound.InboundCommunicationHandler;
import com.energyict.mdc.engine.impl.web.events.EventServlet;
import com.energyict.mdc.engine.impl.web.events.WebSocketEventPublisherFactory;
import com.energyict.mdc.engine.impl.web.queryapi.QueryApiServlet;

import org.eclipse.jetty.server.HttpConnection;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.server.ServerConnector;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ByteArrayISO8859Writer;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.joda.time.DateTimeConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.security.KeyStore;
import java.security.KeyStoreException;
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

    private final ShutdownFailureLogger shutdownFailureLogger;

    public interface ServiceProvider {

        public WebSocketEventPublisherFactory webSocketEventPublisherFactory();

    }

    /**
     * The number of seconds that accepted requests are allowed to complete
     * during the graceful shutdown.
     */
    private static final int GRACEFUL_SHUTDOWN_SECONDS = 3;
    private Server jetty;

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

    private EmbeddedJettyServer (ServletBasedInboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, InboundCommunicationHandler.ServiceProvider serviceProvider)
             {
        super();
        this.jetty = new Server();
        this.shutdownFailureLogger = new ComPortShutdownFailureLogger(comPort);
        if (comPort.isHttps()) {
            try {
                SslContextFactory sslContextFactory = new SslContextFactory();
                sslContextFactory.setKeyStorePath(comPort.getKeyStoreSpecsFilePath());
                sslContextFactory.setKeyStorePassword(comPort.getKeyStoreSpecsPassword());
                KeyStore keyStore = KeyStore.getInstance(comPort.getTrustStoreSpecsFilePath());
                sslContextFactory.setTrustStore(keyStore);
                sslContextFactory.setTrustStorePassword(comPort.getTrustStoreSpecsPassword());
                ServerConnector sslConnector = new ServerConnector(this.jetty, sslContextFactory);
                sslConnector.setPort(comPort.getPortNumber());
                this.jetty.addConnector(sslConnector);
            }catch (KeyStoreException ex){}
        }
        else {
            ServerConnector connector = new ServerConnector(jetty);
            connector.setPort(comPort.getPortNumber());
            this.jetty.addConnector(connector);
        }

        initCustomErrorHandling();

        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        ComServlet servlet = new ComServlet(comPort, comServerDAO, deviceCommandExecutor, serviceProvider);
        handler.addServlet(new ServletHolder(servlet), this.getContextPath(comPort));
        this.jetty.setHandler(handler);
    }

    private void initCustomErrorHandling() {

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

    /**
     * Creates a new EmbeddedJettyServer that will host the servlet for the event mechanism.
     *
     * @param eventRegistrationUri The URI on which the servlet should be listening
     * @param serviceProvider The ServiceProvider
     */
    public static EmbeddedJettyServer newForEventMechanism (URI eventRegistrationUri, ServiceProvider serviceProvider) {
        EmbeddedJettyServer server = new EmbeddedJettyServer(new EventMechanismShutdownFailureLogger(eventRegistrationUri));
        server.addEventMechanism(eventRegistrationUri, serviceProvider);
        return server;
    }

    public void addEventMechanism (URI eventRegistrationUri, ServiceProvider serviceProvider) {
        this.jetty = new Server(getPortNumber(eventRegistrationUri, ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER));
        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        ServletHolder servletHolder = new ServletHolder(new EventServlet(serviceProvider.webSocketEventPublisherFactory()));
        handler.addServlet(servletHolder, eventRegistrationUri.getPath());
        this.jetty.setHandler(handler);
    }

    /**
     * Creates a new EmbeddedJettyServer that will host
     * the servlet for the remote query api of the specified
     * {@link OnlineComServer online comserver}.
     *
     * @param queryApiPostUri The URI on which the servlet should be listening
     * @param comServer The OnlineComServer
     */
    public static EmbeddedJettyServer newForQueryApi (URI queryApiPostUri, RunningOnlineComServer comServer) {
        EmbeddedJettyServer server = new EmbeddedJettyServer(new QueryAPIShutdownFailureLogger(queryApiPostUri));
        server.addQueryApi(queryApiPostUri, comServer);
        return server;
    }

    public void addQueryApi (URI queryApiPostUri, RunningOnlineComServer comServer) {
        this.jetty = new Server(getPortNumber(queryApiPostUri, ComServer.DEFAULT_QUERY_API_PORT_NUMBER));
        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        ServletHolder servletHolder = new ServletHolder(new QueryApiServlet(comServer));
        handler.addServlet(servletHolder, queryApiPostUri.getPath());
        this.jetty.setHandler(handler);
    }

    private EmbeddedJettyServer (ShutdownFailureLogger shutdownFailureLogger) {
        super();
        this.shutdownFailureLogger = shutdownFailureLogger;
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
        try {
            this.jetty.start();
        }
        catch (Exception e) {
            e.printStackTrace(System.err);
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
                // this.jetty.setGracefulShutdown(DateTimeConstants.MILLIS_PER_SECOND * GRACEFUL_SHUTDOWN_SECONDS);
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