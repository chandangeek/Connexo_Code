package com.energyict.mdc.engine.impl.web;

import com.elster.jupiter.users.User;
import com.energyict.mdc.engine.config.ServletBasedInboundComPort;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.inbound.InboundCommunicationHandler;
import com.energyict.mdc.engine.impl.core.inbound.InboundDiscoveryContextImpl;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol;
import com.energyict.mdc.protocol.api.inbound.ServletBasedInboundDeviceProtocol;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides a general purpose servlet implementation that will
 * support inbound communication for ServletBasedInboundComPorts.
 * It is assuming that the InboundDeviceProtocol
 * is <strong>ALWAYS</strong> returning
 * so that this servlet does not do handover to the
 * {@link DeviceProtocol}
 * of the Device that is posting the data.
 * The following roughly describes the sequence of actions:
 * <ul>
 * <li>Check with the {@link DeviceCommandExecutor} if additional tasks can be accepted</li>
 * <li>Execute the InboundDeviceProtocol</li>
 * <li>Filter the {@link com.energyict.mdc.protocol.api.device.data.CollectedData} against the {@link com.energyict.mdc.tasks.ComTask}s of the Device that is posting the data</li>
 * <li>Convert the filtered collected data to {@link com.energyict.mdc.engine.impl.commands.store.DeviceCommand}s</li>
 * <li>Execute the composite device command with the DeviceCommandExecutor</li>
 * </ul>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-11 (16:29)
 */
public class ComServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ComServlet.class.getName());
    private final InboundCommunicationHandler.ServiceProvider serviceProvider;

    private Statistics statistics = new Statistics();
    private InboundCommunicationHandler communicationHandler;
    private ServletBasedInboundComPort comPort;
    private ComServerDAO comServerDAO;

    public ComServlet(ServletBasedInboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, InboundCommunicationHandler.ServiceProvider serviceProvider) {
        super();
        this.communicationHandler = new InboundCommunicationHandler(comPort, comServerDAO, deviceCommandExecutor, serviceProvider);
        this.comPort = comPort;
        this.comServerDAO = comServerDAO;
        this.serviceProvider = serviceProvider;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter responseWriter = response.getWriter();
        responseWriter.println("<HTML><BODY><H1>ComServer servlet based com port connector for ComPort " + this.comPort.getName() + "</H1><TABLE>");
        responseWriter.println("<TR><TD>Jupiter version:</TD><TD>" + this.getJupiterVersion() + "</TD></TR>");
        responseWriter.println("<TR><TD>Servlet version:</TD><TD>" + getWebVersion() + "</TD></TR>");
        this.statistics.printWith(responseWriter);
        responseWriter.println("</TABLE></BODY></HTML>");
        responseWriter.close();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws
            IOException,
            ServletException {
        this.setThreadPrinciple();
        this.statistics.doPost();
        try {
            this.handOverToInboundDeviceProtocol(request, response);
        } catch (Throwable t) {
            // Avoid that the current thread will stop because of e.g. NPE
            LOGGER.log(Level.SEVERE, t.getMessage(), t);
        }
    }

    private void setThreadPrinciple() {
        Optional<User> user = this.serviceProvider.userService().findUser("batch executor");
        if (user.isPresent()) {
            this.serviceProvider.threadPrincipalService().set(user.get(), "ComServlet", "doPost", Locale.ENGLISH);
        }
    }

    private void handOverToInboundDeviceProtocol(HttpServletRequest request, HttpServletResponse response) {
        ServletBasedInboundDeviceProtocol inboundDeviceProtocol = this.newInboundDeviceProtocol();
        InboundDiscoveryContextImpl context = this.newInboundDiscoveryContext(request, response);
        inboundDeviceProtocol.initializeDiscoveryContext(context);
        inboundDeviceProtocol.init(request, response);
        this.communicationHandler.handle(inboundDeviceProtocol, context);
        this.checkForConfigurationError(this.communicationHandler.getResponseType());
    }

    private void checkForConfigurationError(InboundDeviceProtocol.DiscoverResponseType responseType) {
        switch (responseType) {
            case DEVICE_NOT_FOUND: {
                // Intentional fallthrough
            }
            case DEVICE_DOES_NOT_EXPECT_INBOUND: {
                // Intentional fallthrough
            }
            case ENCRYPTION_REQUIRED: {
                this.statistics.configurationErrorCount++;
                break;
            }
            case SUCCESS: {
                // Intentional fallthrough
            }
            case SERVER_BUSY: {
                // Intentional fallthrough
            }
            case STORING_FAILURE: {
                // Intentional fallthrough
            }
            default: {
                // Does not count as a configuration error
            }
        }
    }

    private InboundDiscoveryContextImpl newInboundDiscoveryContext(HttpServletRequest request, HttpServletResponse response) {
        InboundDiscoveryContextImpl context = new InboundDiscoveryContextImpl(this.comPort, request, response, serviceProvider.connectionTaskService());
        context.setInboundDAO(this.comServerDAO);
        context.setLogger(Logger.getAnonymousLogger());
        return context;
    }

    private ServletBasedInboundDeviceProtocol newInboundDeviceProtocol() {
        return (ServletBasedInboundDeviceProtocol) this.comPort.getComPortPool().getDiscoveryProtocolPluggableClass().getInboundDeviceProtocol();
    }

    private String getJupiterVersion() {
        return "1.0.0-SNAPSHOT";
    }

    private String getWebVersion() {
        String version = getVersion();
        return this.getClass().getName() + " " + version.substring(7, version.length() - 2);
    }

    public String getVersion() {
        return "$Date: 2012-10-11 17:21:47 +0200 $";
    }

    private class Statistics {
        /**
         * Number of calls to the doPost method of this Servlet.
         */
        private long hitCount;

        /* Number of times a call was made for an Device
         * that is not configured for inbound communication
         * or the ComPortPool of the inbound connection task
         * does not match with the ComPort of this Servlet.
         */
        private long configurationErrorCount;

        public void doPost() {
            this.hitCount++;
        }

        public void printWith(PrintWriter writer) {
            writer.println("<TR><TD>Servlet hitcount :</TD><TD>" + this.hitCount + "</TD></TR>");
            writer.println("<TR><TD>Communication configuration error count:</TD><TD>" + this.configurationErrorCount + "</TD></TR>");
        }
    }

}