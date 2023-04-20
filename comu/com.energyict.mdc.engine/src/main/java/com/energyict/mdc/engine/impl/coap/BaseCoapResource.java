/*
 * Copyright (c) 2022 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.coap;

import com.elster.jupiter.users.User;
import com.energyict.mdc.common.comserver.CoapBasedInboundComPort;
import com.energyict.mdc.common.protocol.InboundDeviceProtocol;
import com.energyict.mdc.common.protocol.InboundDeviceProtocolPluggableClass;
import com.energyict.mdc.engine.impl.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.inbound.InboundCommunicationHandler;
import com.energyict.mdc.engine.impl.core.inbound.InboundDiscoveryContextImpl;
import com.energyict.mdc.protocol.api.exceptions.NestedPropertyValidationException;
import com.energyict.mdc.protocol.pluggable.adapters.upl.TypedPropertiesValueAdapter;
import com.energyict.mdc.protocol.pluggable.adapters.upl.UPLInboundDeviceProtocolAdapter;
import com.energyict.mdc.upl.CoapBasedInboundDeviceProtocol;
import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.io.CoapBasedExchange;
import com.energyict.mdc.upl.properties.PropertyValidationException;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;

import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BaseCoapResource extends CoapResource {

    private static final Logger LOGGER = Logger.getLogger(BaseCoapResource.class.getName());
    private final InboundCommunicationHandler.ServiceProvider serviceProvider;

    private final DeviceCommandExecutor deviceCommandExecutor;
    private final CoapBasedInboundComPort comPort;
    private final ComServerDAO comServerDAO;

    public BaseCoapResource(CoapBasedInboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, InboundCommunicationHandler.ServiceProvider serviceProvider) {
        super(normalizedContextPath(comPort.getContextPath()));
        this.comPort = comPort;
        this.comServerDAO = comServerDAO;
        this.serviceProvider = serviceProvider;
        this.deviceCommandExecutor = deviceCommandExecutor;
    }

    public static String normalizedContextPath(String contextPath) {
        return contextPath != null ? contextPath.replaceAll("/", "_") : null;
    }

    @Override
    public void handlePOST(CoapExchange exchange) {
        this.setThreadPrinciple();
        try {
            this.handOverToInboundDeviceProtocol(exchange);
        } catch (Exception t) {
            // Avoid that the current thread will stop because of e.g. NPE
            LOGGER.log(Level.SEVERE, t.getMessage(), t);
        }
    }

    private InboundCommunicationHandler getInboundCommunicationHandler() {
        return new InboundCommunicationHandler(comPort, comServerDAO, deviceCommandExecutor, serviceProvider);
    }

    private void setThreadPrinciple() {
        User comServerUser = comServerDAO.getComServerUser();
        this.serviceProvider.threadPrincipalService().set(comServerUser, "ComCoap", "doPost", comServerUser.getLocale().orElse(Locale.ENGLISH));
    }

    private void handOverToInboundDeviceProtocol(CoapExchange coapExchange) {
        CoapBasedExchange exchange = new CoapBasedExchangeImpl(coapExchange);
        CoapBasedInboundDeviceProtocol inboundDeviceProtocol = this.newInboundDeviceProtocol();
        InboundDiscoveryContextImpl context = this.newInboundDiscoveryContext(exchange);
        inboundDeviceProtocol.initializeDiscoveryContext(context);
        inboundDeviceProtocol.init(exchange);
        InboundCommunicationHandler inboundCommunicationHandler = getInboundCommunicationHandler();
        inboundCommunicationHandler.handle(inboundDeviceProtocol, context);
    }

    private InboundDiscoveryContextImpl newInboundDiscoveryContext(CoapBasedExchange exchange) {
        InboundDiscoveryContextImpl context = new InboundDiscoveryContextImpl(this.comPort, exchange, serviceProvider.connectionTaskService());
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

    private CoapBasedInboundDeviceProtocol newInboundDeviceProtocol() {
        InboundDeviceProtocolPluggableClass discoveryProtocolPluggableClass = this.comPort.getComPortPool().getDiscoveryProtocolPluggableClass();
        InboundDeviceProtocol inboundDeviceProtocol = discoveryProtocolPluggableClass.getInboundDeviceProtocol();

        com.energyict.mdc.upl.InboundDeviceProtocol uplInboundDeviceProtocol;
        if (inboundDeviceProtocol instanceof UPLInboundDeviceProtocolAdapter) {
            uplInboundDeviceProtocol = ((UPLInboundDeviceProtocolAdapter) inboundDeviceProtocol).getUplInboundDeviceProtocol();
        } else {
            uplInboundDeviceProtocol = inboundDeviceProtocol;
        }

        try {
            inboundDeviceProtocol.setUPLProperties(TypedPropertiesValueAdapter.adaptToUPLValues(comPort.getComPortPool().getTypedProperties()));
        } catch (PropertyValidationException e) {
            throw new NestedPropertyValidationException(e);
        }

        if (!(uplInboundDeviceProtocol instanceof CoapBasedInboundDeviceProtocol)) {
            throw new IllegalStateException(serviceProvider.thesaurus().getFormat(MessageSeeds.INVALID_INBOUND_SERVLET_PROTOCOL).format(discoveryProtocolPluggableClass.getJavaClassName()));
        }

        return (CoapBasedInboundDeviceProtocol) uplInboundDeviceProtocol;
    }
}