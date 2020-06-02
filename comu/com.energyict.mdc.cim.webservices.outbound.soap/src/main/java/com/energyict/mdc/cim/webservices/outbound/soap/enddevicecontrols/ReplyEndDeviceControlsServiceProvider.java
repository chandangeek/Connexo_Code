/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.outbound.soap.enddevicecontrols;

import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;

import com.energyict.mdc.cim.webservices.outbound.soap.ReplyEndDeviceControlsWebService;

import ch.iec.tc57._2011.enddevicecontrolsmessage.EndDeviceControlsResponseMessageType;
import ch.iec.tc57._2011.replyenddevicecontrols.EndDeviceControlsPort;
import ch.iec.tc57._2011.replyenddevicecontrols.ReplyEndDeviceControls;
import ch.iec.tc57._2011.schema.message.HeaderType;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.xml.ws.Service;
import java.util.Map;
import java.util.UUID;

@Component(name = "com.energyict.mdc.cim.webservices.outbound.soap.replyenddevicecontrols.provider",
        service = {ReplyEndDeviceControlsWebService.class, OutboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + ReplyEndDeviceControlsWebService.NAME})
public class ReplyEndDeviceControlsServiceProvider extends AbstractOutboundEndPointProvider<EndDeviceControlsPort>
        implements ReplyEndDeviceControlsWebService, OutboundSoapEndPointProvider, ApplicationSpecific {

    private static final String NOUN = "EndDeviceControls";

    private final ch.iec.tc57._2011.schema.message.ObjectFactory cimMessageObjectFactory
            = new ch.iec.tc57._2011.schema.message.ObjectFactory();
    private final ch.iec.tc57._2011.enddevicecontrolsmessage.ObjectFactory endDeviceControlsMessageObjectFactory
            = new ch.iec.tc57._2011.enddevicecontrolsmessage.ObjectFactory();

    public ReplyEndDeviceControlsServiceProvider() {
        // for OSGi purposes
    }

    @Reference
    public void addWebServicesService(WebServicesService webServicesService) {
        // Just to inject WebServicesService
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addEndDeviceControlsPortPort(EndDeviceControlsPort endDeviceControlsPort, Map<String, Object> properties) {
        super.doAddEndpoint(endDeviceControlsPort, properties);
    }

    public void removeEndDeviceControlsPortPort(EndDeviceControlsPort endDeviceControlsPort) {
        super.doRemoveEndpoint(endDeviceControlsPort);
    }


    @Override
    public Service get() {
        return new ReplyEndDeviceControls();
    }

    @Override
    public Class<EndDeviceControlsPort> getService() {
        return EndDeviceControlsPort.class;
    }

    @Override
    protected String getName() {
        return ReplyEndDeviceControlsWebService.NAME;
    }

    @Override
    public String getApplication() {
        return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
    }

    @Override
    public void call() {
        EndDeviceControlsResponseMessageType message = createResponseMessage();

        // TODO: stub

        using("createdEndDeviceControls")
                //.withRelatedAttributes(values)
                .send(message);
    }

    private EndDeviceControlsResponseMessageType createResponseMessage() {
        EndDeviceControlsResponseMessageType responseMessage = endDeviceControlsMessageObjectFactory.createEndDeviceControlsResponseMessageType();

        // set header
        HeaderType header = cimMessageObjectFactory.createHeaderType();
        header.setVerb(HeaderType.Verb.CREATED);
        header.setNoun(NOUN);
        header.setCorrelationID(UUID.randomUUID().toString());
        responseMessage.setHeader(header);

        return responseMessage;
    }
}
