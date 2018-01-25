/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.outbound.soap.enddeviceevents;

import com.elster.jupiter.issue.share.OutboundEndDeviceEventsWebServiceClient;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.energyict.mdc.device.alarms.entity.OpenDeviceAlarm;

import ch.iec.tc57._2011.enddeviceevents.Asset;
import ch.iec.tc57._2011.enddeviceevents.EndDeviceEvent;
import ch.iec.tc57._2011.enddeviceevents.EndDeviceEvents;
import ch.iec.tc57._2011.enddeviceeventsmessage.EndDeviceEventsEventMessageType;
import ch.iec.tc57._2011.enddeviceeventsmessage.EndDeviceEventsPayloadType;
import ch.iec.tc57._2011.sendenddeviceevents.EndDeviceEventsPort;
import ch.iec.tc57._2011.sendenddeviceevents.SendEndDeviceEvents;
import ch.iec.tc57._2011.schema.message.HeaderType;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.xml.ws.Service;
import java.util.ArrayList;
import java.util.List;

@Component(name = "com.energyict.mdc.cim.webservices.outbound.soap.enddeviceevents.provider",
        service = {OutboundEndDeviceEventsWebServiceClient.class, OutboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + OutboundEndDeviceEventsWebServiceClient.NAME})
public class EndDeviceEventsServiceProvider implements OutboundEndDeviceEventsWebServiceClient, OutboundSoapEndPointProvider {
    private static final String END_DEVICE_EVENTS = "EndDeviceEvents";
    private static final String END_DEVICE_EVENT_SEVERITY = "Alarm";

    private final ch.iec.tc57._2011.schema.message.ObjectFactory cimMessageObjectFactory
            = new ch.iec.tc57._2011.schema.message.ObjectFactory();
    private final ch.iec.tc57._2011.enddeviceeventsmessage.ObjectFactory endDeviceEventsMessageObjectFactory
            = new ch.iec.tc57._2011.enddeviceeventsmessage.ObjectFactory();

    private List<EndDeviceEventsPort> endDeviceEvents = new ArrayList<>();

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addReplyEndDeviceEvents(EndDeviceEventsPort events) {
        endDeviceEvents.add(events);
    }

    public void removeReplyEndDeviceEvents(EndDeviceEventsPort events) {
        endDeviceEvents.remove(events);
    }

    @Override
    public Service get() {
        return new SendEndDeviceEvents(this.getClass().getResource("/enddeviceevents/SendEndDeviceEvents.wsdl"));
    }

    @Override
    public Class getService() {
        return EndDeviceEventsPort.class;
    }

    @Override
    public String getWebServiceName() {
        return OutboundEndDeviceEventsWebServiceClient.NAME;
    }

    @Override
    public void call(Issue issue, List<EndPointConfiguration> endPointConfigurations) {
        endPointConfigurations.stream().forEach(endPointConfiguration -> {
            endDeviceEvents.stream().forEach(event -> {
                try {
                    event.createdEndDeviceEvents(createResponseMessage(issue));
                } catch (Exception e) {
                    endPointConfiguration.log(String.format("Failed to send %s to web service %s with the URL: %s",
                            END_DEVICE_EVENTS, endPointConfiguration.getWebServiceName(), endPointConfiguration.getUrl()), e);
                }
            });
        });
    }

    private EndDeviceEventsEventMessageType createResponseMessage(Issue issue) {
        EndDeviceEventsEventMessageType responseMessage = endDeviceEventsMessageObjectFactory.createEndDeviceEventsEventMessageType();

        // set header
        HeaderType header = cimMessageObjectFactory.createHeaderType();
        header.setVerb(HeaderType.Verb.CREATED);
        header.setNoun(END_DEVICE_EVENTS);
        responseMessage.setHeader(header);

        // set payload
        EndDeviceEventsPayloadType endDeviceEventsPayload = endDeviceEventsMessageObjectFactory.createEndDeviceEventsPayloadType();
        EndDeviceEvents endDeviceEvents = new EndDeviceEvents();
        endDeviceEvents.getEndDeviceEvent().add(createEndDeviceEvent(issue));
        endDeviceEventsPayload.setEndDeviceEvents(endDeviceEvents);
        responseMessage.setPayload(endDeviceEventsPayload);

        return responseMessage;
    }

    private EndDeviceEvent createEndDeviceEvent(Issue issue) {
        EndDevice device = issue.getDevice();
        EndDeviceEvent endDeviceEvent = new EndDeviceEvent();
        endDeviceEvent.setMRID(device.getMRID());
        endDeviceEvent.setCreatedDateTime(issue.getCreateDateTime());
        endDeviceEvent.setIssuerID(issue.getIssueId());
        endDeviceEvent.setReason(issue.getReason().getName());
        endDeviceEvent.setSeverity(END_DEVICE_EVENT_SEVERITY);
        endDeviceEvent.setUserID(issue.getUserName());

        Asset asset = new Asset();
        asset.setMRID(device.getMRID());
        endDeviceEvent.setAssets(asset);

        if (issue instanceof OpenDeviceAlarm) {
            OpenDeviceAlarm alarm = (OpenDeviceAlarm) issue;
            alarm.getDeviceAlarmRelatedEvents().stream().findFirst().ifPresent(event -> {
                EndDeviceEventRecord record = event.getEventRecord();
                EndDeviceEvent.EndDeviceEventType eventType = new EndDeviceEvent.EndDeviceEventType();
                eventType.setRef(record.getEventTypeCode());
                endDeviceEvent.setEndDeviceEventType(eventType);
                endDeviceEvent.setIssuerTrackingID(record.getIssuerTrackingID());
            });
        }
        return endDeviceEvent;
    }
}
