/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.outbound.soap.enddeviceevents;

import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataExportWebService;
import com.elster.jupiter.export.ExportData;
import com.elster.jupiter.export.MeterEventData;
import com.elster.jupiter.issue.share.IssueWebServiceClient;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.metering.CimAttributeNames;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.elster.jupiter.util.streams.Predicates;
import com.energyict.mdc.cim.webservices.outbound.soap.EndDeviceEventsServiceProvider;
import com.energyict.mdc.cim.webservices.outbound.soap.impl.MessageSendingFailed;
import com.energyict.mdc.cim.webservices.outbound.soap.impl.TranslationInstaller;
import com.energyict.mdc.device.alarms.entity.OpenDeviceAlarm;

import ch.iec.tc57._2011.enddeviceevents.Asset;
import ch.iec.tc57._2011.enddeviceevents.EndDeviceEvent;
import ch.iec.tc57._2011.enddeviceevents.EndDeviceEventDetail;
import ch.iec.tc57._2011.enddeviceevents.EndDeviceEvents;
import ch.iec.tc57._2011.enddeviceevents.Name;
import ch.iec.tc57._2011.enddeviceevents.NameType;
import ch.iec.tc57._2011.enddeviceeventsmessage.EndDeviceEventsEventMessageType;
import ch.iec.tc57._2011.enddeviceeventsmessage.EndDeviceEventsPayloadType;
import ch.iec.tc57._2011.enddeviceeventsmessage.EndDeviceEventsResponseMessageType;
import ch.iec.tc57._2011.schema.message.ErrorType;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.schema.message.ReplyType;
import ch.iec.tc57._2011.sendenddeviceevents.EndDeviceEventsPort;
import ch.iec.tc57._2011.sendenddeviceevents.SendEndDeviceEvents;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Inject;
import javax.xml.ws.Service;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component(name = "com.energyict.mdc.cim.webservices.outbound.soap.enddeviceevents.provider",
        service = {DataExportWebService.class, EndDeviceEventsServiceProvider.class, IssueWebServiceClient.class, OutboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + EndDeviceEventsServiceProvider.NAME})
public class EndDeviceEventsServiceProviderImpl extends AbstractOutboundEndPointProvider<EndDeviceEventsPort>
        implements DataExportWebService, EndDeviceEventsServiceProvider, IssueWebServiceClient, OutboundSoapEndPointProvider, ApplicationSpecific {
    private static final String END_DEVICE_EVENTS = "EndDeviceEvents";
    private static final String END_DEVICE_NAME_TYPE = "EndDevice";
    private static final String DEVICE_PROTOCOL_CODE_LABEL = "DeviceProtocolCode";

    private final ch.iec.tc57._2011.schema.message.ObjectFactory cimMessageObjectFactory
            = new ch.iec.tc57._2011.schema.message.ObjectFactory();
    private final ch.iec.tc57._2011.enddeviceeventsmessage.ObjectFactory endDeviceEventsMessageObjectFactory
            = new ch.iec.tc57._2011.enddeviceeventsmessage.ObjectFactory();

    private volatile Thesaurus thesaurus;
    private volatile EndPointConfigurationService endPointConfigurationService;

    public EndDeviceEventsServiceProviderImpl() {
        // for OSGi purposes
    }

    // for tests
    @Inject
    public EndDeviceEventsServiceProviderImpl(Thesaurus thesaurus, EndPointConfigurationService endPointConfigurationService) {
        this.thesaurus = thesaurus;
        setEndPointConfigurationService(endPointConfigurationService);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addEndpoint(EndDeviceEventsPort events, Map<String, Object> properties) {
        super.doAddEndpoint(events, properties);
    }

    public void removeEndpoint(EndDeviceEventsPort events) {
        super.doRemoveEndpoint(events);
    }

    @Reference
    public void setEndPointConfigurationService(EndPointConfigurationService endPointConfigurationService) {
        this.endPointConfigurationService = endPointConfigurationService;
    }

    @Reference
    public void setDataExportService(DataExportService dataExportService) {
        // just a dependency
    }

    @Reference
    public void setThesaurus(TranslationInstaller translationInstaller) {
        this.thesaurus = translationInstaller.getThesaurus();
    }

    @Override
    public Service get() {
        return new SendEndDeviceEvents(this.getClass().getResource("/wsdl/enddeviceevents/SendEndDeviceEvents.wsdl"));
    }

    @Override
    public Class<EndDeviceEventsPort> getService() {
        return EndDeviceEventsPort.class;
    }

    @Override
    public void call(EndPointConfiguration endPointConfiguration, Stream<? extends ExportData> data, ExportContext context) {
        List<MeterEventData> eventDataList = data.filter(MeterEventData.class::isInstance)
                .map(MeterEventData.class::cast)
                .collect(Collectors.toList());

        if (!eventDataList.isEmpty()) {
            EndDeviceEventsEventMessageType responseMessage = endDeviceEventsMessageObjectFactory.createEndDeviceEventsEventMessageType();

            // set header
            responseMessage.setHeader(getHeader());

            // set payload
            EndDeviceEventsPayloadType endDeviceEventsPayload = endDeviceEventsMessageObjectFactory.createEndDeviceEventsPayloadType();
            EndDeviceEvents endDeviceEvents = new EndDeviceEvents();

            SetMultimap<String, String> values = HashMultimap.create();
            eventDataList.forEach(eventData -> {
                List<String> structurePath = eventData.getStructureMarker().getStructurePath();
                if (structurePath.size() > 1) {
                    String deviceMrid = structurePath.get(0);
                    String deviceName = structurePath.get(1);

                    eventData.getMeterReading().getEvents().forEach(event -> endDeviceEvents.getEndDeviceEvent()
                            .add(createEndDeviceEvent(event, deviceMrid, deviceName)));
                    values.put(CimAttributeNames.CIM_DEVICE_MR_ID.getAttributeName(),
                            deviceMrid);
                    values.put(CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(),
                            deviceName);
                } else {
                    // should never occur
                    throw new IllegalArgumentException("Event data doesn't contain device info.");
                }
            });

            if (!endDeviceEvents.getEndDeviceEvent().isEmpty()) {
                endDeviceEventsPayload.setEndDeviceEvents(endDeviceEvents);
                responseMessage.setPayload(endDeviceEventsPayload);
                Map<EndPointConfiguration, ?> response = using("createdEndDeviceEvents")
                        .withRelatedAttributes(values)
                        .toEndpoints(endPointConfiguration)
                        .send(responseMessage);

                if (!extractResult((EndDeviceEventsResponseMessageType) response.get(endPointConfiguration)).filter(ReplyType.Result.OK::equals).isPresent()) {
                    throw new MessageSendingFailed(thesaurus, endPointConfiguration);
                }
            }
        }
    }

    private EndDeviceEvent createEndDeviceEvent(com.elster.jupiter.metering.readings.EndDeviceEvent event, String endDeviceMrid, String endDeviceName) {
        return convertEndDeviceEvent(endDeviceMrid, endDeviceName, event);
    }

    private HeaderType getHeader() {
        HeaderType header = cimMessageObjectFactory.createHeaderType();
        header.setVerb(HeaderType.Verb.CREATED);
        header.setNoun(END_DEVICE_EVENTS);
        header.setCorrelationID(UUID.randomUUID().toString());
        return header;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getSupportedDataType() {
        return DataExportService.STANDARD_EVENT_DATA_TYPE;
    }

    @Override
    public Set<Operation> getSupportedOperations() {
        return EnumSet.of(Operation.CREATE);
    }

    @Override
    public String getWebServiceName() {
        return getName();
    }

    @Override
    public boolean call(Issue issue, EndPointConfiguration endPointConfiguration) {
        EndDeviceEventsEventMessageType message = createResponseMessage(issue);
        using("createdEndDeviceEvents")
                .toEndpoints(endPointConfiguration)
                .send(message);
        return true;
    }

    private EndDeviceEventsEventMessageType createResponseMessage(Issue issue) {
        EndDeviceEventsEventMessageType responseMessage = endDeviceEventsMessageObjectFactory.createEndDeviceEventsEventMessageType();

        // set header
        responseMessage.setHeader(getHeader());

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
        endDeviceEvent.setAssets(createAsset(device.getMRID(), device.getName()));
        if (issue instanceof OpenDeviceAlarm && !((OpenDeviceAlarm) issue).getDeviceAlarmRelatedEvents().isEmpty()) {
            EndDeviceEventRecord record = ((OpenDeviceAlarm) issue).getDeviceAlarmRelatedEvents().get(0).getEventRecord();
            return convertEndDeviceEvent(device.getMRID(), device.getName(), record);
        }
        return endDeviceEvent;
    }

    @Override
    public void call(EndDeviceEventRecord record, EndPointConfiguration... endPointConfigurations) {
        EndDeviceEventsEventMessageType message = createResponseMessage(record);

        SetMultimap<String, String> values = HashMultimap.create();
        values.put(CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(),
                record.getEndDevice().getName());
        values.put(CimAttributeNames.CIM_DEVICE_MR_ID.getAttributeName(),
                record.getEndDevice().getMRID());

        Set<EndPointConfiguration> requestedEndPoints = endPointConfigurations.length == 0 ?
                new HashSet<>(endPointConfigurationService.getEndPointConfigurationsForWebService(EndDeviceEventsServiceProvider.NAME)) :
                Arrays.stream(endPointConfigurations).collect(Collectors.toSet());
        RequestSender sender = using("createdEndDeviceEvents")
                .withRelatedAttributes(values);
        if (!requestedEndPoints.isEmpty()) {
            sender = sender.toEndpoints(requestedEndPoints);
        }
        Set<EndPointConfiguration> processedEndPoints = sender.send(message)
                .entrySet()
                .stream()
                .filter(entry -> extractResult((EndDeviceEventsResponseMessageType) entry.getValue()).filter(ReplyType.Result.OK::equals).isPresent())
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        EndPointConfiguration[] unprocessedEndPoints = requestedEndPoints.stream()
                .filter(Predicates.not(processedEndPoints::contains))
                .toArray(EndPointConfiguration[]::new);
        if (unprocessedEndPoints.length != 0) {
            throw new MessageSendingFailed(thesaurus, unprocessedEndPoints);
        }
    }

    private static Optional<ReplyType.Result> extractResult(EndDeviceEventsResponseMessageType response) {
        return Optional.ofNullable(response)
                .map(EndDeviceEventsResponseMessageType::getReply)
                .map(ReplyType::getResult);
    }

    @Override
    public void call(List<EndDeviceEvent> events, List<ErrorType> errorTypes,
                     EndPointConfiguration endPointConfiguration, String correlationId) {
        EndDeviceEventsEventMessageType message = createResponseMessage(events, errorTypes, correlationId);

        SetMultimap<String, String> values = HashMultimap.create();
        events.forEach(event -> {
            values.put(CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(), event.getAssets().getNames().get(0).getName());
            values.put(CimAttributeNames.CIM_DEVICE_MR_ID.getAttributeName(), event.getAssets().getMRID());
        });

        using("createdEndDeviceEvents")
                .toEndpoints(endPointConfiguration)
                .withRelatedAttributes(values)
                .send(message);
    }

    private EndDeviceEventsEventMessageType createResponseMessage(EndDeviceEventRecord record) {
        EndDeviceEventsEventMessageType responseMessage = endDeviceEventsMessageObjectFactory.createEndDeviceEventsEventMessageType();

        // set header
        responseMessage.setHeader(createHeader(UUID.randomUUID().toString()));

        // set payload
        EndDeviceEventsPayloadType endDeviceEventsPayload = endDeviceEventsMessageObjectFactory.createEndDeviceEventsPayloadType();
        EndDeviceEvents endDeviceEvents = new EndDeviceEvents();
        endDeviceEvents.getEndDeviceEvent().add(createEndDeviceEvent(record));
        endDeviceEventsPayload.setEndDeviceEvents(endDeviceEvents);
        responseMessage.setPayload(endDeviceEventsPayload);

        return responseMessage;
    }

    private EndDeviceEventsEventMessageType createResponseMessage(List<EndDeviceEvent> events, List<ErrorType> errorTypes,
                                                                  String correlationId) {
        EndDeviceEventsEventMessageType responseMessage = endDeviceEventsMessageObjectFactory.createEndDeviceEventsEventMessageType();

        // set header
        responseMessage.setHeader(createHeader(correlationId));

        // set reply
        ReplyType replyType = cimMessageObjectFactory.createReplyType();
        ReplyType.Result result;

        if (!errorTypes.isEmpty() && !events.isEmpty()) {
            result = ReplyType.Result.PARTIAL;
        } else if (errorTypes.isEmpty()) {
            result = ReplyType.Result.OK;
        } else {
            result = ReplyType.Result.FAILED;
        }
        replyType.getError().addAll(errorTypes);
        replyType.setResult(result);
        responseMessage.setReply(replyType);

        // set payload
        if (!events.isEmpty()) {
            EndDeviceEvents endDeviceEvents = new EndDeviceEvents();
            endDeviceEvents.getEndDeviceEvent().addAll(events);
            EndDeviceEventsPayloadType endDeviceEventsPayload = endDeviceEventsMessageObjectFactory.createEndDeviceEventsPayloadType();

            endDeviceEventsPayload.setEndDeviceEvents(endDeviceEvents);
            responseMessage.setPayload(endDeviceEventsPayload);
        }

        return responseMessage;
    }

    private HeaderType createHeader(String correlationId) {
        HeaderType header = cimMessageObjectFactory.createHeaderType();
        header.setVerb(HeaderType.Verb.CREATED);
        header.setNoun(END_DEVICE_EVENTS);
        header.setCorrelationID(correlationId);
        return header;
    }

    private EndDeviceEvent createEndDeviceEvent(EndDeviceEventRecord record) {
        EndDevice device = record.getEndDevice();
        return convertEndDeviceEvent(device.getMRID(), device.getName(), record);
    }

    private EndDeviceEvent convertEndDeviceEvent(String endDeviceMrid, String endDeviceName, com.elster.jupiter.metering.readings.EndDeviceEvent event) {
        EndDeviceEvent endDeviceEvent = new EndDeviceEvent();
        endDeviceEvent.setAssets(createAsset(endDeviceMrid, endDeviceName));
        endDeviceEvent.setMRID(event.getMRID());
        endDeviceEvent.setCreatedDateTime(event.getCreatedDateTime());
        endDeviceEvent.setIssuerID(event.getIssuerID());
        endDeviceEvent.setIssuerTrackingID(event.getIssuerTrackingID());
        endDeviceEvent.setReason(event.getReason());
        endDeviceEvent.setUserID(event.getUserID());
        endDeviceEvent.setSeverity(event.getSeverity());
        event.getEventData().forEach((key, value) -> {
                    EndDeviceEventDetail endDeviceEventDetail = new EndDeviceEventDetail();
                    endDeviceEventDetail.setName(key);
                    endDeviceEventDetail.setValue(value);
                    endDeviceEvent.getEndDeviceEventDetails().add(endDeviceEventDetail);
                }
        );
        createDeviceEventTypeDetail(event).ifPresent(endDeviceEvent.getEndDeviceEventDetails()::add);
        EndDeviceEvent.EndDeviceEventType eventType = new EndDeviceEvent.EndDeviceEventType();
        eventType.setRef(event.getEventTypeCode());
        endDeviceEvent.setEndDeviceEventType(eventType);

        return endDeviceEvent;
    }

    private Optional<EndDeviceEventDetail> createDeviceEventTypeDetail(com.elster.jupiter.metering.readings.EndDeviceEvent event) {
        String deviceEventType = event.getType();
        if (Objects.nonNull(deviceEventType) && !deviceEventType.isEmpty()) {
            EndDeviceEventDetail endDeviceEventDetail = new EndDeviceEventDetail();
            endDeviceEventDetail.setName(DEVICE_PROTOCOL_CODE_LABEL);
            endDeviceEventDetail.setValue(deviceEventType);
            return Optional.of(endDeviceEventDetail);
        }
        return Optional.empty();
    }

    private Asset createAsset(String endDeviceMrid, String endDeviceName) {
        Asset asset = new Asset();
        asset.setMRID(endDeviceMrid);
        asset.getNames().add(createName(endDeviceName));
        return asset;
    }

    private Name createName(String endDeviceName) {
        NameType nameType = new NameType();
        nameType.setName(END_DEVICE_NAME_TYPE);
        Name name = new Name();
        name.setNameType(nameType);
        name.setName(endDeviceName);
        return name;
    }

    @Override
    public String getApplication() {
        return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
    }
}
