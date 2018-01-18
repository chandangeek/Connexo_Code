/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.outbound.soap.impl;

import com.elster.jupiter.issue.share.CreatedEndDeviceEventsWebServiceClient;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;

import ch.iec.tc57._2011.enddeviceevents.EndDeviceEvent;
import ch.iec.tc57._2011.enddeviceevents.EndDeviceEvents;
import ch.iec.tc57._2011.enddeviceevents.FaultMessage;
import ch.iec.tc57._2011.enddeviceevents.ReplyEndDeviceEvents;
import ch.iec.tc57._2011.enddeviceevents.ReplyEndDeviceEventsPort;
import ch.iec.tc57._2011.enddeviceeventsmessage.EndDeviceEventsEventMessageType;
import ch.iec.tc57._2011.enddeviceeventsmessage.EndDeviceEventsPayloadType;
import ch.iec.tc57._2011.schema.message.HeaderType;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.xml.ws.Service;
import java.util.ArrayList;
import java.util.List;

@Component(name = "com.energyict.mdc.cim.webservices.outbound.soap.enddeviceevents.provider",
        service = {CreatedEndDeviceEventsWebServiceClient.class, OutboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + CreatedEndDeviceEventsWebServiceClient.NAME})
public class EndDeviceEventsServiceProvider implements CreatedEndDeviceEventsWebServiceClient, OutboundSoapEndPointProvider {
    private static final String CREATED_END_DEVICE_EVENTS = "CreatedEndDeviceEvents";

    private final ch.iec.tc57._2011.schema.message.ObjectFactory cimMessageObjectFactory
            = new ch.iec.tc57._2011.schema.message.ObjectFactory();
    private final ch.iec.tc57._2011.enddeviceeventsmessage.ObjectFactory endDeviceEventsMessageObjectFactory
            = new ch.iec.tc57._2011.enddeviceeventsmessage.ObjectFactory();

    private List<ReplyEndDeviceEventsPort> endDeviceEvents = new ArrayList<>();

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addReplyEndDeviceEvents(ReplyEndDeviceEventsPort events) {
        endDeviceEvents.add(events);
    }

    public void removeReplyEndDeviceEvents(ReplyEndDeviceEventsPort events) {
        endDeviceEvents.remove(events);
    }

    @Override
    public Service get() {
        return new ReplyEndDeviceEvents();
    }

    @Override
    public Class getService() {
        return ReplyEndDeviceEventsPort.class;
    }

    @Override
    public String getWebServiceName() {
        return CreatedEndDeviceEventsWebServiceClient.NAME;
    }

    @Override
    public boolean call(Issue issue, EndPointConfiguration endPointConfiguration) {
        endDeviceEvents.stream().forEach(event -> {
            try {
                event.createdEndDeviceEvents(createResponseMessage(issue));
            } catch (FaultMessage faultMessage) {
                faultMessage.printStackTrace();
            }
        });
        return false;
    }

    private EndDeviceEventsEventMessageType createResponseMessage(Issue issue) {
        EndDeviceEventsEventMessageType responseMessage = endDeviceEventsMessageObjectFactory.createEndDeviceEventsEventMessageType();

        // set header
        HeaderType header = cimMessageObjectFactory.createHeaderType();
        header.setVerb("Reply");
        header.setNoun(CREATED_END_DEVICE_EVENTS);
        responseMessage.setHeader(header);


        // set payload
        EndDeviceEventsPayloadType endDeviceEventsPayload = endDeviceEventsMessageObjectFactory.createEndDeviceEventsPayloadType();
        EndDeviceEvents endDeviceEvents = new EndDeviceEvents();
        EndDeviceEvent endDeviceEvent = new EndDeviceEvent();
        endDeviceEvent.setSeverity(issue.getPriority().toString());
        endDeviceEvent.setCreatedDateTime(issue.getCreateDateTime());
        endDeviceEvent.setIssuerID(issue.getIssueId());
        endDeviceEvent.setMRID(issue.getTitle());
        endDeviceEvents.getEndDeviceEvent().add(endDeviceEvent);

        endDeviceEventsPayload.setEndDeviceEvents(endDeviceEvents);
        responseMessage.setPayload(endDeviceEventsPayload);
        return responseMessage;
    }
}
