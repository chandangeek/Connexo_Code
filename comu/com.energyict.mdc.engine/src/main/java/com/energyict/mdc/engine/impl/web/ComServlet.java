/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.web;

import com.elster.jupiter.users.User;
import com.energyict.mdc.common.comserver.ServletBasedInboundComPort;
import com.energyict.mdc.common.protocol.DeviceProtocol;
import com.energyict.mdc.common.protocol.InboundDeviceProtocol;
import com.energyict.mdc.common.protocol.InboundDeviceProtocolPluggableClass;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.engine.impl.EngineServiceImpl;
import com.energyict.mdc.engine.impl.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.inbound.InboundCommunicationHandler;
import com.energyict.mdc.engine.impl.core.inbound.InboundDiscoveryContextImpl;
import com.energyict.mdc.protocol.pluggable.adapters.upl.UPLInboundDeviceProtocolAdapter;
import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.ServletBasedInboundDeviceProtocol;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
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
 * <li>Filter the {@link com.energyict.mdc.upl.meterdata.CollectedData} against the {@link ComTask}s of the Device that is posting the data</li>
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

    private AtomicReference<Statistics> statistics = new AtomicReference<>();
    private AtomicReference<InboundCommunicationHandler> communicationHandler;
    private ServletBasedInboundComPort comPort;
    private ComServerDAO comServerDAO;

    public ComServlet(ServletBasedInboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, InboundCommunicationHandler.ServiceProvider serviceProvider) {
        super();
        this.communicationHandler = new AtomicReference<>(new InboundCommunicationHandler(comPort, comServerDAO, deviceCommandExecutor, serviceProvider));
        this.comPort = comPort;
        this.comServerDAO = comServerDAO;
        this.serviceProvider = serviceProvider;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter responseWriter = response.getWriter();
        responseWriter.println("<HTML><BODY><H1>ComServer servlet based com port connector for ComPort " + this.comPort.getName() + "</H1><TABLE>");
        responseWriter.println("<TR><TD>Connexo version:</TD><TD>" + this.getJupiterVersion() + "</TD></TR>");
        responseWriter.println("<TR><TD>Servlet version:</TD><TD>" + getWebVersion() + "</TD></TR>");
        responseWriter.println("<TR><TD>Connexo system name:</TD><TD style='color:" + getSystemIdentifierColor() + "'>" + getSystemIdentifier() + "</TD></TR>");
        responseWriter.println("<TR><TD>ComServer name:</TD><TD>" +  getComServerName() + "</TD></TR>");
        this.statistics.get().printWith(responseWriter);
        responseWriter.println("</TABLE></BODY></HTML>");
        responseWriter.close();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws
            IOException,
            ServletException {
        this.setThreadPrinciple();
        this.statistics.get().doPost();
        try {
            this.handOverToInboundDeviceProtocol(request, response);
        } catch (Exception t) {
            // Avoid that the current thread will stop because of e.g. NPE
            LOGGER.log(Level.SEVERE, t.getMessage(), t);
        }
    }

    private void setThreadPrinciple() {
        User comServerUser = comServerDAO.getComServerUser();
        this.serviceProvider.threadPrincipalService().set(comServerUser, "ComServlet", "doPost", comServerUser.getLocale().orElse(Locale.ENGLISH));
    }

    private void handOverToInboundDeviceProtocol(HttpServletRequest request, HttpServletResponse response) {
        ServletBasedInboundDeviceProtocol inboundDeviceProtocol = this.newInboundDeviceProtocol();
        InboundDiscoveryContextImpl context = this.newInboundDiscoveryContext(request, response);
        inboundDeviceProtocol.initializeDiscoveryContext(context);
        inboundDeviceProtocol.init(request, response);
        this.communicationHandler.get().handle(inboundDeviceProtocol, context);
        this.checkForConfigurationError(this.communicationHandler.get().getResponseType());
    }

    private void checkForConfigurationError(com.energyict.mdc.upl.InboundDeviceProtocol.DiscoverResponseType responseType) {
        switch (responseType) {
            case DEVICE_NOT_FOUND: {
                // Intentional fallthrough
            }
            case DEVICE_DOES_NOT_EXPECT_INBOUND: {
                // Intentional fallthrough
            }
            case ENCRYPTION_REQUIRED: {
                this.statistics.get().configurationErrorCount++;
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
            case DATA_ONLY_PARTIALLY_HANDLED: {
                // Intentional fallthrough
            }
            default: {
                // Does not count as a configuration error
            }
        }
    }

    private InboundDiscoveryContextImpl newInboundDiscoveryContext(HttpServletRequest request, HttpServletResponse response) {
        InboundDiscoveryContextImpl context = new InboundDiscoveryContextImpl(this.comPort, request, response, serviceProvider.connectionTaskService());
        context.setComServerDAO(this.comServerDAO);
        context.setLogger(Logger.getAnonymousLogger());

        context.setObjectMapperService(Services.objectMapperService());
        context.setCollectedDataFactory(Services.collectedDataFactory());
        context.setIssueFactory(Services.issueFactory());
        context.setPropertySpecService(Services.propertySpecService());
        context.setNlsService(Services.nlsService());
        context.setConverter(Services.converter());
        context.setDeviceGroupExtractor(Services.deviceGroupExtractor());
        context.setDeviceExtractor(Services.deviceExtractor());
        context.setDeviceMasterDataExtractor(Services.deviceMasterDataExtractor());
        context.setMessageFileExtractor(Services.deviceMessageFileExtractor());
        context.setCertificateWrapperExtractor(Services.certificateWrapperExtractor());
        context.setKeyAccessorTypeExtractor(Services.keyAccessorTypeExtractor());
        context.setHsmProtocolService(Services.hsmService());

        return context;
    }

    private ServletBasedInboundDeviceProtocol newInboundDeviceProtocol() {
        InboundDeviceProtocolPluggableClass discoveryProtocolPluggableClass = this.comPort.getComPortPool().getDiscoveryProtocolPluggableClass();
        InboundDeviceProtocol inboundDeviceProtocol = discoveryProtocolPluggableClass.getInboundDeviceProtocol();

        com.energyict.mdc.upl.InboundDeviceProtocol uplInboundDeviceProtocol;
        if (inboundDeviceProtocol instanceof UPLInboundDeviceProtocolAdapter) {
            uplInboundDeviceProtocol = ((UPLInboundDeviceProtocolAdapter) inboundDeviceProtocol).getUplInboundDeviceProtocol();
        } else {
            uplInboundDeviceProtocol = inboundDeviceProtocol;
        }

        if (uplInboundDeviceProtocol instanceof ServletBasedInboundDeviceProtocol) {
            return ((ServletBasedInboundDeviceProtocol) uplInboundDeviceProtocol);
        } else {
            throw new IllegalStateException(serviceProvider.thesaurus().getFormat(MessageSeeds.INVALID_INBOUND_SERVLET_PROTOCOL).format(discoveryProtocolPluggableClass.getJavaClassName()));
        }

    }

    private String getJupiterVersion() {
        return String.valueOf(EngineServiceImpl.engineProperties.get(EngineServiceImpl.SYSTEM_VERSION));
    }

    private String getWebVersion() {
        String version = getVersion();
        return this.getClass().getName() + " " + version.substring(7, version.length() - 2);
    }

    public String getVersion() {
        return "$Date: 2012-10-11 17:21:47 +0200 $";
    }

    private String getSystemIdentifier () {
       return String.valueOf(EngineServiceImpl.engineProperties.get(EngineServiceImpl.SYSTEM_IDENTIFIER));
    }

    private String getSystemIdentifierColor () {
        return String.valueOf(EngineServiceImpl.engineProperties.get(EngineServiceImpl.SYSTEM_IDENTIFIER_COLOR));
    }

    private String getComServerName() {
        if(comServerDAO.getThisComServer() != null)
            return comServerDAO.getThisComServer().getName();
        return "-";
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