/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.getenddeviceevents;

import com.elster.jupiter.domain.util.VerboseConstraintViolationException;
import com.elster.jupiter.metering.CimAttributeNames;
import com.elster.jupiter.metering.CimUsagePointAttributeNames;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractInboundEndPoint;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.ReplyTypeFactory;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.ServiceCallCommands;

import ch.iec.tc57._2011.enddeviceevents.EndDeviceEvents;
import ch.iec.tc57._2011.getenddeviceevents.EndDeviceEventType;
import ch.iec.tc57._2011.getenddeviceevents.EndDeviceGroup;
import ch.iec.tc57._2011.getenddeviceevents.FaultMessage;
import ch.iec.tc57._2011.getenddeviceevents.GetEndDeviceEvents;
import ch.iec.tc57._2011.getenddeviceevents.GetEndDeviceEventsPort;
import ch.iec.tc57._2011.getenddeviceevents.Meter;
import ch.iec.tc57._2011.getenddeviceeventsmessage.EndDeviceEventsPayloadType;
import ch.iec.tc57._2011.getenddeviceeventsmessage.EndDeviceEventsResponseMessageType;
import ch.iec.tc57._2011.getenddeviceeventsmessage.GetEndDeviceEventsRequestMessageType;
import ch.iec.tc57._2011.schema.message.HeaderType;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Range;
import com.google.common.collect.SetMultimap;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class GetEndDeviceEventsEndpoint extends AbstractInboundEndPoint implements GetEndDeviceEventsPort , ApplicationSpecific {

    private static final String GET_END_DEVICE_EVENTS = "GetEndDeviceEvents";
    private static final String METERS_ITEM = GET_END_DEVICE_EVENTS + ".Meters";
    private static final String METERS_AND_DEVICE_GROUPS_ITEM = METERS_ITEM + "/" + GET_END_DEVICE_EVENTS + ".EndDeviceGroups";

    private final ch.iec.tc57._2011.schema.message.ObjectFactory cimMessageObjectFactory
            = new ch.iec.tc57._2011.schema.message.ObjectFactory();
    private final ch.iec.tc57._2011.getenddeviceeventsmessage.ObjectFactory endDeviceEventsMessageObjectFactory
            = new ch.iec.tc57._2011.getenddeviceeventsmessage.ObjectFactory();

    private final Clock clock;
    private final ReplyTypeFactory replyTypeFactory;
    private final EndDeviceEventsFaultMessageFactory messageFactory;
    private final EndDeviceEventsBuilder endDeviceBuilder;
    private final ServiceCallCommands serviceCallCommands;
    private final EndPointConfigurationService endPointConfigurationService;
    private final WebServicesService webServicesService;

    @Inject
    GetEndDeviceEventsEndpoint(ReplyTypeFactory replyTypeFactory,
                               EndDeviceEventsFaultMessageFactory messageFactory,
                               EndDeviceEventsBuilder endDeviceBuilder, Clock clock,
                               ServiceCallCommands serviceCallCommands, EndPointConfigurationService endPointConfigurationService,
                               WebServicesService webServicesService) {
        this.replyTypeFactory = replyTypeFactory;
        this.messageFactory = messageFactory;
        this.endDeviceBuilder = endDeviceBuilder;
        this.clock = clock;
        this.serviceCallCommands = serviceCallCommands;
        this.endPointConfigurationService = endPointConfigurationService;
        this.webServicesService = webServicesService;
    }

    @Override
    public EndDeviceEventsResponseMessageType getEndDeviceEvents(GetEndDeviceEventsRequestMessageType requestMessage) throws FaultMessage {
        return runInTransactionWithOccurrence(() -> {
            try {
                SetMultimap<String, String> values = HashMultimap.create();
                requestMessage.getRequest().getGetEndDeviceEvents().getMeter().forEach(meter->{
                    if (!meter.getNames().isEmpty()) {
                        values.put(CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(), meter.getNames().get(0).getName());
                    }
                    if (meter.getMRID() != null){
                        values.put(CimAttributeNames.CIM_DEVICE_MR_ID.getAttributeName(), meter.getMRID());
                    }
                    if (meter.getSerialNumber() != null){
                        values.put(CimAttributeNames.CIM_DEVICE_SERIAL_NUMBER.getAttributeName(), meter.getSerialNumber());
                    }

                });
                requestMessage.getRequest().getGetEndDeviceEvents().getUsagePoint().forEach(usp->{
                    if (!usp.getNames().isEmpty()){
                        values.put(CimUsagePointAttributeNames.CIM_USAGE_POINT_NAME.getAttributeName(), usp.getNames().get(0).getName());
                    }
                    if (usp.getMRID() != null){
                        values.put(CimUsagePointAttributeNames.CIM_USAGE_POINT_MR_ID.getAttributeName(), usp.getMRID());
                    }
                });

                saveRelatedAttributes(values);

                GetEndDeviceEvents getEndDeviceEvents = Optional.ofNullable(requestMessage.getRequest().getGetEndDeviceEvents())
                        .orElseThrow(messageFactory.createEndDeviceEventsFaultMessageSupplier(MessageSeeds.MISSING_ELEMENT, GET_END_DEVICE_EVENTS));
                List<Meter> meters = getEndDeviceEvents.getMeter();
                List<EndDeviceGroup> deviceGroups = getEndDeviceEvents.getEndDeviceGroup();
                List<EndDeviceEventType> eventTypes = getEndDeviceEvents.getEndDeviceEventType();

                String correlationId = requestMessage.getHeader() == null ? null : requestMessage.getHeader().getCorrelationID();
                Boolean asyncReplyFlag = requestMessage.getHeader() == null ? null : requestMessage.getHeader().isAsyncReplyFlag();

                if (Boolean.TRUE.equals(asyncReplyFlag)) {
                    // call asynchronously
                    if (deviceGroups.isEmpty() && meters.isEmpty()) {
                        throw messageFactory.createEndDeviceEventsFaultMessageSupplier(MessageSeeds.EMPTY_LIST, METERS_AND_DEVICE_GROUPS_ITEM).get();
                    }
                    EndPointConfiguration outboundEndPointConfiguration = getOutboundEndPointConfiguration(getReplyAddress(requestMessage));
                    createServiceCallAndTransition(meters, deviceGroups, eventTypes, endDeviceBuilder.getTimeIntervals(getEndDeviceEvents.getTimeSchedule()), outboundEndPointConfiguration, correlationId);
                    return createQuickResponseMessage(correlationId);
                } else if (meters.size() > 1 || !deviceGroups.isEmpty()) {
                    throw messageFactory.createEndDeviceEventsFaultMessage(MessageSeeds.SYNC_MODE_NOT_SUPPORTED);
                } else {
                    // call synchronously
                    if (meters.isEmpty()) {
                        throw messageFactory.createEndDeviceEventsFaultMessageSupplier(MessageSeeds.EMPTY_LIST, METERS_ITEM).get();
                    }
                    EndDeviceEvents endDeviceEvents = endDeviceBuilder.prepareGetFrom(meters, getEndDeviceEvents.getTimeSchedule()).setEndDeviceEventTypeFilters(eventTypes).build();
                    return createResponseMessage(endDeviceEvents, correlationId);
                }
            } catch (VerboseConstraintViolationException e) {
                throw messageFactory.createEndDeviceEventsFaultMessage(e.getLocalizedMessage());
            } catch (LocalizedException e) {
                throw messageFactory.createEndDeviceEventsFaultMessage(e.getLocalizedMessage(), e.getErrorCode());
            }
        });
    }

    private String getReplyAddress(GetEndDeviceEventsRequestMessageType requestMessage) throws FaultMessage {
        String replyAddress = requestMessage.getHeader().getReplyAddress();
        if (Checks.is(replyAddress).emptyOrOnlyWhiteSpace()) {
            throw messageFactory.createEndDeviceEventsFaultMessage(MessageSeeds.NO_REPLY_ADDRESS);
        }
        return replyAddress;
    }

    private EndPointConfiguration getOutboundEndPointConfiguration(String url) throws FaultMessage {
        EndPointConfiguration endPointConfig = endPointConfigurationService.getOutboundEndpointConfigurationByUrl(url)
                .filter(EndPointConfiguration::isActive)
                .orElseThrow(messageFactory.createEndDeviceEventsFaultMessageSupplier(MessageSeeds.NO_END_POINT_WITH_URL, url));
        if (!webServicesService.isPublished(endPointConfig)) {
            webServicesService.publishEndPoint(endPointConfig);
        }
        if (!webServicesService.isPublished(endPointConfig)) {
            throw messageFactory.createEndDeviceEventsFaultMessageSupplier(MessageSeeds.NO_PUBLISHED_END_POINT_WITH_URL, url).get();
        }
        return endPointConfig;
    }

    private ServiceCall createServiceCallAndTransition(List<Meter> meters,List<EndDeviceGroup> deviceGroups, List<EndDeviceEventType> eventTypes,
                                                       Range<Instant> interval, EndPointConfiguration endPointConfiguration, String correlationId) throws FaultMessage {
        ServiceCall serviceCall = serviceCallCommands.createGetEndDeviceEventsMasterServiceCall(meters, deviceGroups, eventTypes, interval, endPointConfiguration, correlationId);
        serviceCallCommands.requestTransition(serviceCall, DefaultState.PENDING);
        return serviceCall;
    }

    private EndDeviceEventsResponseMessageType createResponseMessage(EndDeviceEvents endDeviceEvents, String correlationId) {
        EndDeviceEventsResponseMessageType responseMessage = createQuickResponseMessage(correlationId);

        // set payload
        EndDeviceEventsPayloadType endDeviceEventsPayload = endDeviceEventsMessageObjectFactory.createEndDeviceEventsPayloadType();
        endDeviceEventsPayload.setEndDeviceEvents(endDeviceEvents);
        responseMessage.setPayload(endDeviceEventsPayload);

        return responseMessage;
    }

    private EndDeviceEventsResponseMessageType createQuickResponseMessage(String correlationId) {
        EndDeviceEventsResponseMessageType responseMessage = endDeviceEventsMessageObjectFactory.createEndDeviceEventsResponseMessageType();

        // set header
        HeaderType header = cimMessageObjectFactory.createHeaderType();
        header.setVerb(HeaderType.Verb.REPLY);
        header.setNoun(GET_END_DEVICE_EVENTS);
        header.setCorrelationID(correlationId);

        responseMessage.setHeader(header);

        // set reply
        responseMessage.setReply(replyTypeFactory.okReplyType());

        return responseMessage;
    }

    @Override
    public String getApplication(){
        return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
    }
}
