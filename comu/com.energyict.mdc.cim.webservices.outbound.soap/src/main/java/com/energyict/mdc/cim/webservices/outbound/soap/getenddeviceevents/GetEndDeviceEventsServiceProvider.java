/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.outbound.soap.getenddeviceevents;

import com.elster.jupiter.cbo.Status;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.energyict.mdc.cim.webservices.inbound.soap.ReplyGetEndDeviceEventsWebService;

import ch.iec.tc57._2011.enddeviceevents.EndDeviceEvent;
import ch.iec.tc57._2011.enddeviceevents.EndDeviceEventDetail;
import ch.iec.tc57._2011.enddeviceevents.EndDeviceEvents;
import ch.iec.tc57._2011.enddeviceevents.ObjectFactory;
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
import java.util.ArrayList;
import java.util.List;

@Component(name = "com.energyict.mdc.cim.webservices.outbound.soap.getenddeviceevents.provider",
        service = {ReplyGetEndDeviceEventsWebService.class, OutboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + ReplyGetEndDeviceEventsWebService.NAME})
public class GetEndDeviceEventsServiceProvider implements ReplyGetEndDeviceEventsWebService, OutboundSoapEndPointProvider {

    private static final String GET_END_DEVICE_EVENTS = "GetEndDeviceEvents";

    private final ch.iec.tc57._2011.schema.message.ObjectFactory cimMessageObjectFactory
            = new ch.iec.tc57._2011.schema.message.ObjectFactory();
    private final ch.iec.tc57._2011.getenddeviceeventsmessage.ObjectFactory getEndDeviceEventsMessageObjectFactory
            = new ch.iec.tc57._2011.getenddeviceeventsmessage.ObjectFactory();

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

    @Override
    public Service get() {
        return new GetEndDeviceEvents_Service(this.getClass().getResource("/getenddeviceevents/ReplyGetEndDeviceEvents.wsdl"));
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

    // todo mlevan

    private final ObjectFactory payloadObjectFactory = new ObjectFactory();

    private EndDeviceEvents createEndDeviceEvents(List<EndDeviceEventRecord> records) {
        EndDeviceEvents endDeviceEvents = new EndDeviceEvents();
        List<EndDeviceEvent> endDeviceEventList = endDeviceEvents.getEndDeviceEvent();
        records.stream().forEach(record -> {
            EndDeviceEvent endDeviceEvent = payloadObjectFactory.createEndDeviceEvent();
            endDeviceEvent.setEndDeviceEventType(toEndDeviceEventType(record.getEventType()));
            endDeviceEvent.setMRID(record.getMRID());
            endDeviceEvent.setCreatedDateTime(record.getCreatedDateTime());
            endDeviceEvent.setIssuerID(record.getIssuerID());
            endDeviceEvent.setIssuerTrackingID(record.getIssuerTrackingID());
            endDeviceEvent.setSeverity(record.getSeverity());
            endDeviceEvent.setStatus(toStatus(record.getStatus()));
            endDeviceEvent.setReason(record.getDescription());

            record.getProperties().entrySet().stream().forEach(property -> {
                        EndDeviceEventDetail endDeviceEventDetail = payloadObjectFactory.createEndDeviceEventDetail();
                        endDeviceEventDetail.setName(property.getKey());
                        endDeviceEventDetail.setValue(property.getValue());
                        endDeviceEvent.getEndDeviceEventDetails().add(endDeviceEventDetail);
                    }
            );
            endDeviceEventList.add(endDeviceEvent);
        });
        return endDeviceEvents;
    }

    public EndDeviceEvent.EndDeviceEventType toEndDeviceEventType(com.elster.jupiter.metering.events.EndDeviceEventType eventType) {
        EndDeviceEvent.EndDeviceEventType type = payloadObjectFactory.createEndDeviceEventEndDeviceEventType();
        type.setRef(eventType.getMRID());
        return type;
    }

    public ch.iec.tc57._2011.enddeviceevents.Status toStatus(Status status) {
        if (status == null) {
            return null;
        }
        ch.iec.tc57._2011.enddeviceevents.Status state = payloadObjectFactory.createStatus();
        state.setDateTime(status.getDateTime());
        state.setReason(status.getReason());
        state.setRemark(status.getRemark());
        state.setValue(status.getValue());
        return state;
    }
}
