/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.outbound.soap.getenddeviceevents;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.energyict.mdc.cim.webservices.inbound.soap.ReplyGetEndDeviceEventsWebService;

import ch.iec.tc57._2011.enddeviceevents.EndDeviceEvent;
import ch.iec.tc57._2011.enddeviceevents.EndDeviceEvents;
import ch.iec.tc57._2011.getenddeviceevents.FaultMessage;
import ch.iec.tc57._2011.getenddeviceevents.GetEndDeviceEventsPort;
import ch.iec.tc57._2011.getenddeviceevents.GetEndDeviceEvents_Service;
import ch.iec.tc57._2011.getenddeviceeventsmessage.EndDeviceEventsPayloadType;
import ch.iec.tc57._2011.getenddeviceeventsmessage.GetEndDeviceEventsRequestMessageType;
import ch.iec.tc57._2011.schema.message.HeaderType;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.xml.ws.Service;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

@Component(name = "com.energyict.mdc.cim.webservices.outbound.soap.getenddeviceevents.provider",
        service = {ReplyGetEndDeviceEventsWebService.class, OutboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + ReplyGetEndDeviceEventsWebService.NAME})
public class GetEndDeviceEventsServiceProvider implements ReplyGetEndDeviceEventsWebService, OutboundSoapEndPointProvider {

    private static final String GET_END_DEVICE_EVENTS = "GetEndDeviceEvents";
    private static final String RESOURCE_WSDL = "/getenddeviceevents/GetEndDeviceEvents.wsdl";

    private final ch.iec.tc57._2011.schema.message.ObjectFactory cimMessageObjectFactory
            = new ch.iec.tc57._2011.schema.message.ObjectFactory();
    private final ch.iec.tc57._2011.getenddeviceeventsmessage.ObjectFactory getEndDeviceEventsMessageObjectFactory
            = new ch.iec.tc57._2011.getenddeviceeventsmessage.ObjectFactory();

    private volatile Clock clock;
    private volatile MeteringService meteringService;

    private EndDeviceEventsFactory endDeviceEventsFactory = new EndDeviceEventsFactory();
    private List<GetEndDeviceEventsPort> getEndDeviceEventsPorts = new ArrayList<>();

    public GetEndDeviceEventsServiceProvider() {
        // for OSGI purposes
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addReplyEndDeviceEvents(GetEndDeviceEventsPort port) {
        getEndDeviceEventsPorts.add(port);
    }

    public void removeReplyEndDeviceEvents(GetEndDeviceEventsPort port) {
        getEndDeviceEventsPorts.remove(port);
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Override
    public Service get() {
        return new GetEndDeviceEvents_Service(this.getClass().getResource(RESOURCE_WSDL));
    }

    @Override
    public Class getService() {
        return GetEndDeviceEventsPort.class;
    }

    @Override
    public void call(EndPointConfiguration endPointConfiguration, List<EndDeviceEventRecord> endDeviceEvents) {
        try {
            getEndDeviceEventsPorts.stream()
                    .forEach(portService -> {
                        try {
                            portService.getEndDeviceEvents(createResponseMessage(createEndDeviceEvents(endDeviceEvents)));
                        } catch (FaultMessage faultMessage) {
                            endPointConfiguration.log(faultMessage.getMessage(), faultMessage);
                        }
                    });
        } catch (RuntimeException ex) {
            endPointConfiguration.log(LogLevel.SEVERE, ex.getMessage());
        }
    }

    private GetEndDeviceEventsRequestMessageType createResponseMessage(EndDeviceEvents endDeviceEvents) {
        GetEndDeviceEventsRequestMessageType responseMessage = getEndDeviceEventsMessageObjectFactory.createGetEndDeviceEventsRequestMessageType();

        // set header
        HeaderType header = cimMessageObjectFactory.createHeaderType();
        header.setVerb(HeaderType.Verb.REPLY);
        header.setNoun(GET_END_DEVICE_EVENTS);
        responseMessage.setHeader(header);

        // set payload
        EndDeviceEventsPayloadType endDeviceEventsPayload = getEndDeviceEventsMessageObjectFactory.createEndDeviceEventsPayloadType();
        endDeviceEventsPayload.setEndDeviceEvents(endDeviceEvents);
        responseMessage.setPayload(endDeviceEventsPayload);

        return responseMessage;
    }

    private EndDeviceEvents createEndDeviceEvents(List<EndDeviceEventRecord> records) {
        EndDeviceEvents endDeviceEvents = new EndDeviceEvents();
        List<EndDeviceEvent> endDeviceEventList = endDeviceEvents.getEndDeviceEvent();
        records.stream().forEach(record -> endDeviceEventList.add(endDeviceEventsFactory.asEndDeviceEvent(record)));
        return endDeviceEvents;
    }
}
