package com.energyict.mdc.engine.impl.web;

import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.ServerProcessStatus;
import com.energyict.mdc.engine.impl.web.events.EventServlet;
import com.energyict.mdc.engine.impl.web.queryapi.QueryApiServlet;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.ServletBasedInboundComPort;
import com.energyict.mdc.issues.IssueService;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.ssl.SslSocketConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.joda.time.DateTimeConstants;

import static com.elster.jupiter.util.Checks.is;

/**
 * Provides an implementation for the {@link EmbeddedWebServer} interface
 * for the <a href="http://www.eclipse.org/jetty/">Jetty</a> web server.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-11 (15:43)
 */
public class EmbeddedJettyServer implements EmbeddedWebServer {

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
     *
     * @param comPort The ServerServletBasedInboundComPort
     * @param comServerDAO The ComServerDAO
     * @param deviceCommandExecutor The DeviceCommandExecutor
     * @param issueService The IssueService
     */
    public static EmbeddedJettyServer newForInboundDeviceCommunication(ServletBasedInboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, IssueService issueService) {
        return new EmbeddedJettyServer(comPort, comServerDAO, deviceCommandExecutor, issueService);
    }

    private EmbeddedJettyServer(ServletBasedInboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, IssueService issueService) {
        super();
        this.jetty = new Server();
        if (comPort.isHttps()) {
            SslContextFactory sslContextFactory = new SslContextFactory();
            sslContextFactory.setKeyStorePath(comPort.getKeyStoreSpecsFilePath());
            sslContextFactory.setKeyStorePassword(comPort.getKeyStoreSpecsPassword());
            sslContextFactory.setTrustStore(comPort.getTrustStoreSpecsFilePath());
            sslContextFactory.setTrustStorePassword(comPort.getTrustStoreSpecsPassword());
            SslSocketConnector sslConnector = new SslSocketConnector(sslContextFactory);
            sslConnector.setPort(comPort.getPortNumber());
            this.jetty.addConnector(sslConnector);
        }
        else {
            SelectChannelConnector connector = new SelectChannelConnector();
            connector.setPort(comPort.getPortNumber());
            this.jetty.addConnector(connector);
        }

        initCustomErrorHandling();

        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        ComServlet servlet = new ComServlet(comPort, comServerDAO, deviceCommandExecutor, issueService);
        handler.addServlet(new ServletHolder(servlet), this.getContextPath(comPort));
        this.jetty.setHandler(handler);
    }

    private void initCustomErrorHandling() {
        ErrorHandler errorHandler = new ErrorHandler(){
            @Override
            protected void writeErrorPageBody(HttpServletRequest request, Writer writer, int code, String message, boolean showStacks) throws IOException {
                String uri= request.getRequestURI();
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
        this.jetty.addBean(errorHandler);
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
     * Creates a new EmbeddedJettyServer that will host
     * the servlet for the event mechanism.
     *
     * @param eventRegistrationUri The URI on which the servlet should be listening
     */
    public static EmbeddedJettyServer newForEventMechanism (URI eventRegistrationUri) {
        EmbeddedJettyServer server = new EmbeddedJettyServer();
        server.addEventMechanism(eventRegistrationUri);
        return server;
    }

    public void addEventMechanism (URI eventRegistrationUri) {
        this.jetty = new Server(getPortNumber(eventRegistrationUri, ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER));
        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        handler.addServlet(EventServlet.class, eventRegistrationUri.getPath());
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
    public static EmbeddedJettyServer newForQueryApi (URI queryApiPostUri, OnlineComServer comServer) {
        EmbeddedJettyServer server = new EmbeddedJettyServer();
        server.addQueryApi(queryApiPostUri, comServer);
        return server;
    }

    public void addQueryApi (URI queryApiPostUri, OnlineComServer comServer) {
        this.jetty = new Server(getPortNumber(queryApiPostUri, ComServer.DEFAULT_QUERY_API_PORT_NUMBER));
        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        ServletHolder servletHolder = new ServletHolder(new QueryApiServlet(comServer));
        handler.addServlet(servletHolder, queryApiPostUri.getPath());
        this.jetty.setHandler(handler);
    }

    private EmbeddedJettyServer () {
        super();
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
                this.jetty.setGracefulShutdown(0);
                this.jetty.setStopAtShutdown(false);
            }
            else {
                this.jetty.setGracefulShutdown(DateTimeConstants.MILLIS_PER_SECOND * GRACEFUL_SHUTDOWN_SECONDS);
                this.jetty.setStopAtShutdown(true);
            }
            this.jetty.stop();
        }
        catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

}