/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.getenddeviceevents;

import com.energyict.mdc.cim.webservices.inbound.soap.impl.EndPointHelper;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.ReplyTypeFactory;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.ServiceCallCommands;

import com.elster.jupiter.domain.util.VerboseConstraintViolationException;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.Checks;

import ch.iec.tc57._2011.enddeviceevents.EndDeviceEvents;
import ch.iec.tc57._2011.getenddeviceevents.FaultMessage;
import ch.iec.tc57._2011.getenddeviceevents.GetEndDeviceEvents;
import ch.iec.tc57._2011.getenddeviceevents.GetEndDeviceEventsPort;
import ch.iec.tc57._2011.getenddeviceevents.Meter;
import ch.iec.tc57._2011.getenddeviceeventsmessage.EndDeviceEventsPayloadType;
import ch.iec.tc57._2011.getenddeviceeventsmessage.EndDeviceEventsResponseMessageType;
import ch.iec.tc57._2011.getenddeviceeventsmessage.GetEndDeviceEventsRequestMessageType;
import ch.iec.tc57._2011.schema.message.HeaderType;

import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class GetEndDeviceEventsEndpoint implements GetEndDeviceEventsPort {

    private static final String GET_END_DEVICE_EVENTS = "GetEndDeviceEvents";
    private static final String METERS_ITEM = GET_END_DEVICE_EVENTS + ".Meters";

    private final ch.iec.tc57._2011.schema.message.ObjectFactory cimMessageObjectFactory
            = new ch.iec.tc57._2011.schema.message.ObjectFactory();
    private final ch.iec.tc57._2011.getenddeviceeventsmessage.ObjectFactory endDeviceEventsMessageObjectFactory
            = new ch.iec.tc57._2011.getenddeviceeventsmessage.ObjectFactory();

    private final Clock clock;
    private final EndPointHelper endPointHelper;
    private final ReplyTypeFactory replyTypeFactory;
    private final EndDeviceEventsFaultMessageFactory messageFactory;
    private final TransactionService transactionService;
    private final EndDeviceEventsBuilder endDeviceBuilder;
    private final ServiceCallCommands serviceCallCommands;
    private final EndPointConfigurationService endPointConfigurationService;
    private final WebServicesService webServicesService;

    @Inject
    GetEndDeviceEventsEndpoint(EndPointHelper endPointHelper, ReplyTypeFactory replyTypeFactory,
                               EndDeviceEventsFaultMessageFactory messageFactory, TransactionService transactionService,
                               EndDeviceEventsBuilder endDeviceBuilder, Clock clock,
                               ServiceCallCommands serviceCallCommands, EndPointConfigurationService endPointConfigurationService,
                               WebServicesService webServicesService) {
        this.endPointHelper = endPointHelper;
        this.replyTypeFactory = replyTypeFactory;
        this.messageFactory = messageFactory;
        this.transactionService = transactionService;
        this.endDeviceBuilder = endDeviceBuilder;
        this.clock = clock;
        this.serviceCallCommands = serviceCallCommands;
        this.endPointConfigurationService = endPointConfigurationService;
        this.webServicesService = webServicesService;
    }

    @Override
    public EndDeviceEventsResponseMessageType getEndDeviceEvents(GetEndDeviceEventsRequestMessageType requestMessage) throws FaultMessage {
        endPointHelper.setSecurityContext();
        try (TransactionContext context = transactionService.getContext()) {
            GetEndDeviceEvents getEndDeviceEvents = Optional.ofNullable(requestMessage.getRequest().getGetEndDeviceEvents())
                    .orElseThrow(messageFactory.createEndDeviceEventsFaultMessageSupplier(MessageSeeds.MISSING_ELEMENT, GET_END_DEVICE_EVENTS));
            List<Meter> meters = getEndDeviceEvents.getMeter();
            if (meters.isEmpty()) {
                throw messageFactory.createEndDeviceEventsFaultMessageSupplier(MessageSeeds.EMPTY_LIST, METERS_ITEM).get();
            }
            if (Boolean.TRUE.equals(requestMessage.getHeader().isAsyncReplyFlag())) {
                // call asynchronously
                EndPointConfiguration outboundEndPointConfiguration = getOutboundEndPointConfiguration(getReplyAddress(requestMessage));
                createServiceCallAndTransition(meters, endDeviceBuilder.getTimeIntervals(getEndDeviceEvents.getTimeSchedule()), outboundEndPointConfiguration);
                context.commit();
                return createQuickResponseMessage();
            } else if (meters.size() > 1) {
                throw messageFactory.createEndDeviceEventsFaultMessage(MessageSeeds.SYNC_MODE_NOT_SUPPORTED);
            } else {
                // call synchronously
                EndDeviceEvents endDeviceEvents = endDeviceBuilder.prepareGetFrom(meters, getEndDeviceEvents.getTimeSchedule()).build();
                return createResponseMessage(endDeviceEvents);
            }
        } catch (VerboseConstraintViolationException e) {
            throw messageFactory.createEndDeviceEventsFaultMessage(e.getLocalizedMessage());
        } catch (LocalizedException e) {
            throw messageFactory.createEndDeviceEventsFaultMessage(e.getLocalizedMessage(), e.getErrorCode());
        }
    }

    private String getReplyAddress(GetEndDeviceEventsRequestMessageType requestMessage) throws FaultMessage {
        String replyAddress = requestMessage.getHeader().getReplyAddress();
        if (Checks.is(replyAddress).emptyOrOnlyWhiteSpace()) {
            throw messageFactory.createEndDeviceEventsFaultMessage(MessageSeeds.NO_REPLY_ADDRESS);
        }
        return replyAddress;
    }

    private EndPointConfiguration getOutboundEndPointConfiguration(String url) throws FaultMessage {
        EndPointConfiguration endPointConfig = endPointConfigurationService.findEndPointConfigurations()
                .stream()
                .filter(EndPointConfiguration::isActive)
                .filter(endPointConfiguration -> !endPointConfiguration.isInbound())
                .filter(endPointConfiguration -> endPointConfiguration.getUrl().equals(url))
                .findFirst()
                .orElseThrow(messageFactory.createEndDeviceEventsFaultMessageSupplier(MessageSeeds.NO_END_POINT_WITH_URL, url));
        if (!webServicesService.isPublished(endPointConfig)) {
            webServicesService.publishEndPoint(endPointConfig);
        }
        if (!webServicesService.isPublished(endPointConfig)) {
            throw messageFactory.createEndDeviceEventsFaultMessageSupplier(MessageSeeds.NO_PUBLISHED_END_POINT_WITH_URL, url).get();
        }
        return endPointConfig;
    }

    private ServiceCall createServiceCallAndTransition(List<Meter> meters, Range<Instant> interval, EndPointConfiguration endPointConfiguration) throws FaultMessage {
        ServiceCall serviceCall = serviceCallCommands.createGetEndDeviceEventsMasterServiceCall(meters, interval, endPointConfiguration);
        serviceCallCommands.requestTransition(serviceCall, DefaultState.PENDING);
        return serviceCall;
    }

    private EndDeviceEventsResponseMessageType createResponseMessage(EndDeviceEvents endDeviceEvents) {
        EndDeviceEventsResponseMessageType responseMessage = createQuickResponseMessage();

        // set payload
        EndDeviceEventsPayloadType endDeviceEventsPayload = endDeviceEventsMessageObjectFactory.createEndDeviceEventsPayloadType();
        endDeviceEventsPayload.setEndDeviceEvents(endDeviceEvents);
        responseMessage.setPayload(endDeviceEventsPayload);

        return responseMessage;
    }

    private EndDeviceEventsResponseMessageType createQuickResponseMessage() {
        EndDeviceEventsResponseMessageType responseMessage = endDeviceEventsMessageObjectFactory.createEndDeviceEventsResponseMessageType();

        // set header
        HeaderType header = cimMessageObjectFactory.createHeaderType();
        header.setVerb(HeaderType.Verb.REPLY);
        header.setNoun(GET_END_DEVICE_EVENTS);
        responseMessage.setHeader(header);

        // set reply
        responseMessage.setReply(replyTypeFactory.okReplyType());

        return responseMessage;
    }
}