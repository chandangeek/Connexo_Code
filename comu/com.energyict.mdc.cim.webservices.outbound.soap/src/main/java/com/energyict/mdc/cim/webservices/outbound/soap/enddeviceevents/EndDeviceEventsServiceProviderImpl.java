/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.outbound.soap.enddeviceevents;

import com.elster.jupiter.issue.share.IssueWebServiceClient;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceAplication;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.energyict.mdc.cim.webservices.outbound.soap.EndDeviceEventsServiceProvider;
import com.energyict.mdc.device.alarms.entity.OpenDeviceAlarm;

import ch.iec.tc57._2011.enddeviceevents.Asset;
import ch.iec.tc57._2011.enddeviceevents.EndDeviceEvent;
import ch.iec.tc57._2011.enddeviceevents.EndDeviceEventDetail;
import ch.iec.tc57._2011.enddeviceevents.EndDeviceEvents;
import ch.iec.tc57._2011.enddeviceevents.Name;
import ch.iec.tc57._2011.enddeviceevents.NameType;
import ch.iec.tc57._2011.enddeviceeventsmessage.EndDeviceEventsEventMessageType;
import ch.iec.tc57._2011.enddeviceeventsmessage.EndDeviceEventsPayloadType;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.sendenddeviceevents.EndDeviceEventsPort;
import ch.iec.tc57._2011.sendenddeviceevents.SendEndDeviceEvents;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Inject;
import javax.xml.ws.Service;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component(name = "com.energyict.mdc.cim.webservices.outbound.soap.enddeviceevents.provider",
        service = {EndDeviceEventsServiceProvider.class, IssueWebServiceClient.class, OutboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + EndDeviceEventsServiceProvider.NAME})
public class EndDeviceEventsServiceProviderImpl implements EndDeviceEventsServiceProvider, IssueWebServiceClient, OutboundSoapEndPointProvider, WebServiceAplication {

    private static final Logger LOGGER = Logger.getLogger(EndDeviceEventsServiceProviderImpl.class.getName());
    private static final String END_DEVICE_EVENTS = "EndDeviceEvents";
    private static final String END_DEVICE_NAME_TYPE = "EndDevice";
    private static final String DEVICE_PROTOCOL_CODE_LABEL = "DeviceProtocolCode";

    private final ch.iec.tc57._2011.schema.message.ObjectFactory cimMessageObjectFactory
            = new ch.iec.tc57._2011.schema.message.ObjectFactory();
    private final ch.iec.tc57._2011.enddeviceeventsmessage.ObjectFactory endDeviceEventsMessageObjectFactory
            = new ch.iec.tc57._2011.enddeviceeventsmessage.ObjectFactory();

    private List<EndDeviceEventsPort> endDeviceEvents = new ArrayList<>();

    private volatile WebServicesService webServicesService;

    public EndDeviceEventsServiceProviderImpl() {
        // for OSGI purposes
    }

    @Inject
    public EndDeviceEventsServiceProviderImpl(WebServicesService webServicesService) {
        this();
        setWebServicesService(webServicesService);
    }

    @Reference
    public void setWebServicesService(WebServicesService webServicesService) {
        this.webServicesService = webServicesService;
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addReplyEndDeviceEvents(EndDeviceEventsPort events) {
        endDeviceEvents.add(events);
    }

    public void removeReplyEndDeviceEvents(EndDeviceEventsPort events) {
        endDeviceEvents.remove(events);
    }

    public List<EndDeviceEventsPort> getEndDeviceEventsPorts() {
        return Collections.unmodifiableList(endDeviceEvents);
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
        return NAME;
    }

    @Override
    public boolean call(Issue issue, EndPointConfiguration endPointConfiguration) {
        publish(endPointConfiguration);
        getEndDeviceEventsPorts().forEach(event -> {
            try {
                event.createdEndDeviceEvents(createResponseMessage(issue));
            } catch (Exception e) {
                endPointConfiguration.log(String.format("Failed to send %s to web service %s with the URL: %s",
                        END_DEVICE_EVENTS, endPointConfiguration.getWebServiceName(), endPointConfiguration.getUrl()), e);
            }
        });
        return true;
    }

    private void publish(EndPointConfiguration endPointConfiguration) {
        if (endPointConfiguration.isActive() && !webServicesService.isPublished(endPointConfiguration)) {
            webServicesService.publishEndPoint(endPointConfiguration);
        }
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
        endDeviceEvent.setAssets(createAsset(device));
        if (issue instanceof OpenDeviceAlarm) {
            ((OpenDeviceAlarm) issue).getDeviceAlarmRelatedEvents().stream().findFirst().ifPresent(event -> {
                setEndDeviceEvent(endDeviceEvent, event.getEventRecord());
            });
        }
        return endDeviceEvent;
    }

    @Override
    public void call(EndDeviceEventRecord record) {
        if (! getEndDeviceEventsPorts().isEmpty()) {
            EndDeviceEventsEventMessageType message = createResponseMessage(record);
            getEndDeviceEventsPorts().forEach(event -> {
                try {
                    event.createdEndDeviceEvents(message);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
                }
            });
        } else {
            LOGGER.log(Level.SEVERE, "No published web service endpoint is found to send end device events.");
        }
    }

    private EndDeviceEventsEventMessageType createResponseMessage(EndDeviceEventRecord record) {
        EndDeviceEventsEventMessageType responseMessage = endDeviceEventsMessageObjectFactory.createEndDeviceEventsEventMessageType();

        // set header
        HeaderType header = cimMessageObjectFactory.createHeaderType();
        header.setVerb(HeaderType.Verb.CREATED);
        header.setNoun(END_DEVICE_EVENTS);
        responseMessage.setHeader(header);

        // set payload
        EndDeviceEventsPayloadType endDeviceEventsPayload = endDeviceEventsMessageObjectFactory.createEndDeviceEventsPayloadType();
        EndDeviceEvents endDeviceEvents = new EndDeviceEvents();
        endDeviceEvents.getEndDeviceEvent().add(createEndDeviceEvent(record));
        endDeviceEventsPayload.setEndDeviceEvents(endDeviceEvents);
        responseMessage.setPayload(endDeviceEventsPayload);

        return responseMessage;
    }

    private EndDeviceEvent createEndDeviceEvent(EndDeviceEventRecord record) {
        EndDevice device = record.getEndDevice();
        EndDeviceEvent endDeviceEvent = new EndDeviceEvent();
        endDeviceEvent.setAssets(createAsset(device));
        setEndDeviceEvent(endDeviceEvent, record);
        return endDeviceEvent;
    }

    private void setEndDeviceEvent(EndDeviceEvent endDeviceEvent, EndDeviceEventRecord record) {
        endDeviceEvent.setMRID(record.getMRID());
        endDeviceEvent.setCreatedDateTime(record.getCreatedDateTime());
        endDeviceEvent.setIssuerID(record.getIssuerID());
        endDeviceEvent.setIssuerTrackingID(record.getIssuerTrackingID());
        endDeviceEvent.setReason(record.getDescription());
        endDeviceEvent.setUserID(record.getUserID());
        endDeviceEvent.setSeverity(record.getSeverity());
        record.getProperties().forEach((key, value) -> {
                    EndDeviceEventDetail endDeviceEventDetail = new EndDeviceEventDetail();
                    endDeviceEventDetail.setName(key);
                    endDeviceEventDetail.setValue(value);
                    endDeviceEvent.getEndDeviceEventDetails().add(endDeviceEventDetail);
                }
        );
        createDeviceEventTypeDetail(record).ifPresent(endDeviceEvent.getEndDeviceEventDetails()::add);
        EndDeviceEvent.EndDeviceEventType eventType = new EndDeviceEvent.EndDeviceEventType();
        eventType.setRef(record.getEventTypeCode());
        endDeviceEvent.setEndDeviceEventType(eventType);
    }

    private Optional<EndDeviceEventDetail> createDeviceEventTypeDetail(EndDeviceEventRecord record) {
        String deviceEventType = record.getDeviceEventType();
        if (Objects.nonNull(deviceEventType) && !deviceEventType.isEmpty()) {
            EndDeviceEventDetail endDeviceEventDetail = new EndDeviceEventDetail();
            endDeviceEventDetail.setName(DEVICE_PROTOCOL_CODE_LABEL);
            endDeviceEventDetail.setValue(deviceEventType);
            return Optional.of(endDeviceEventDetail);
        }
        return Optional.empty();
    }

    private Asset createAsset(EndDevice endDevice) {
        Asset asset = new Asset();
        asset.setMRID(endDevice.getMRID());
        asset.getNames().add(createName(endDevice));
        return asset;
    }

    private Name createName(EndDevice endDevice) {
        NameType nameType = new NameType();
        nameType.setName(END_DEVICE_NAME_TYPE);
        Name name = new Name();
        name.setNameType(nameType);
        name.setName(endDevice.getName());
        return name;
    }

    @Override
    public String getApplication(){
        return WebServiceAplication.WebServiceApplicationName.MULTISENSE.getName();
    }
}