/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.getenddeviceevents;

import com.elster.jupiter.domain.util.VerboseConstraintViolationException;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.EndPointHelper;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.ReplyTypeFactory;

import ch.iec.tc57._2011.enddeviceevents.EndDeviceEvents;
import ch.iec.tc57._2011.getenddeviceevents.FaultMessage;
import ch.iec.tc57._2011.getenddeviceevents.GetEndDeviceEvents;
import ch.iec.tc57._2011.getenddeviceevents.GetEndDeviceEventsPort;
import ch.iec.tc57._2011.getenddeviceevents.Meter;
import ch.iec.tc57._2011.getenddeviceeventsmessage.EndDeviceEventsPayloadType;
import ch.iec.tc57._2011.getenddeviceeventsmessage.EndDeviceEventsResponseMessageType;
import ch.iec.tc57._2011.getenddeviceeventsmessage.GetEndDeviceEventsRequestMessageType;
import ch.iec.tc57._2011.schema.message.HeaderType;

import javax.inject.Inject;
import java.time.Clock;
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

    @Inject
    GetEndDeviceEventsEndpoint(EndPointHelper endPointHelper,
                               ReplyTypeFactory replyTypeFactory,
                               EndDeviceEventsFaultMessageFactory messageFactory,
                               TransactionService transactionService,
                               EndDeviceEventsBuilder endDeviceBuilder,
                               Clock clock) {
        this.endPointHelper = endPointHelper;
        this.replyTypeFactory = replyTypeFactory;
        this.messageFactory = messageFactory;
        this.transactionService = transactionService;
        this.endDeviceBuilder = endDeviceBuilder;
        this.clock = clock;
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
            EndDeviceEvents endDeviceEvents = endDeviceBuilder.prepareGetFrom(meters, getEndDeviceEvents.getTimeSchedule()).build();
            return createResponseMessage(endDeviceEvents);
        } catch (VerboseConstraintViolationException e) {
            throw messageFactory.createEndDeviceEventsFaultMessage(e.getLocalizedMessage());
        } catch (LocalizedException e) {
            throw messageFactory.createEndDeviceEventsFaultMessage(e.getLocalizedMessage(), e.getErrorCode());
        }
    }

    private EndDeviceEventsResponseMessageType createResponseMessage(EndDeviceEvents endDeviceEvents) {
        EndDeviceEventsResponseMessageType responseMessage = endDeviceEventsMessageObjectFactory.createEndDeviceEventsResponseMessageType();

        // set header
        HeaderType header = cimMessageObjectFactory.createHeaderType();
        header.setVerb(HeaderType.Verb.REPLY);
        header.setNoun(GET_END_DEVICE_EVENTS);
        responseMessage.setHeader(header);

        // set reply
        responseMessage.setReply(replyTypeFactory.okReplyType());

        // set payload
        EndDeviceEventsPayloadType endDeviceEventsPayload = endDeviceEventsMessageObjectFactory.createEndDeviceEventsPayloadType();
        endDeviceEventsPayload.setEndDeviceEvents(endDeviceEvents);
        responseMessage.setPayload(endDeviceEventsPayload);
        return responseMessage;
    }
}

