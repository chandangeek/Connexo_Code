package com.energyict.mdc.cim.webservices.inbound.soap.enddeviceevents;

import com.elster.jupiter.domain.util.VerboseConstraintViolationException;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointProp;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.EndPointHelper;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.ReplyTypeFactory;
import com.energyict.mdc.dynamic.ObisCodeValueFactory;

import ch.iec.tc57._2011.enddeviceevents.EndDeviceEvent;
import ch.iec.tc57._2011.enddeviceevents.EndDeviceEvents;
import ch.iec.tc57._2011.enddeviceeventsmessage.EndDeviceEventsEventMessageType;
import ch.iec.tc57._2011.enddeviceeventsmessage.EndDeviceEventsPayloadType;
import ch.iec.tc57._2011.enddeviceeventsmessage.EndDeviceEventsResponseMessageType;
import ch.iec.tc57._2011.receiveenddeviceevents.EndDeviceEventsPort;
import ch.iec.tc57._2011.receiveenddeviceevents.FaultMessage;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.schema.message.ReplyType;
import com.google.common.collect.ImmutableList;

import javax.inject.Inject;
import java.util.List;

public class ExecuteEndDeviceEventsEndpoint implements EndDeviceEventsPort, EndPointProp {
    private static final String NOUN = "EndDeviceEvents";
    private static final String END_DEVICE_EVENT_ITEM = NOUN + ".EndDeviceEvent";

    private final EndPointHelper endPointHelper;
    private final ReplyTypeFactory replyTypeFactory;
    private final EndDeviceEventsFaultMessageFactory messageFactory;
    private final TransactionService transactionService;
    private final EndDeviceEventsBuilder endDeviceBuilder;
    private final EndDeviceEventsFactory endDeviceEventsFactory;

    private final ch.iec.tc57._2011.schema.message.ObjectFactory cimMessageObjectFactory
            = new ch.iec.tc57._2011.schema.message.ObjectFactory();
    private final ch.iec.tc57._2011.enddeviceeventsmessage.ObjectFactory endDeviceEventsMessageObjectFactory
            = new ch.iec.tc57._2011.enddeviceeventsmessage.ObjectFactory();

    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;

    @Inject
    ExecuteEndDeviceEventsEndpoint(EndPointHelper endPointHelper,
                                   ReplyTypeFactory replyTypeFactory,
                                   EndDeviceEventsFaultMessageFactory messageFactory,
                                   TransactionService transactionService,
                                   EndDeviceEventsBuilder endDeviceBuilder,
                                   EndDeviceEventsFactory endDeviceEventsFactory,
                                   PropertySpecService propertySpecService,
                                   Thesaurus thesaurus) {
        this.endPointHelper = endPointHelper;
        this.replyTypeFactory = replyTypeFactory;
        this.messageFactory = messageFactory;
        this.transactionService = transactionService;
        this.endDeviceBuilder = endDeviceBuilder;
        this.endDeviceEventsFactory = endDeviceEventsFactory;
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    @Override
    public EndDeviceEventsResponseMessageType createdEndDeviceEvents(EndDeviceEventsEventMessageType createdEndDeviceEventsEventMessage) throws FaultMessage {
        endPointHelper.setSecurityContext();
        try (TransactionContext context = transactionService.getContext()) {
            List<EndDeviceEvent> endDeviceEvents = retrieveEndDeviceEvents(createdEndDeviceEventsEventMessage.getPayload(), MessageSeeds.INVALID_CREATED_END_DEVICE_EVENTS);
            EndDeviceEvent endDeviceEvent = endDeviceEvents.stream().findFirst()
                    .orElseThrow(messageFactory.createEndDeviceEventsFaultMessageSupplier(MessageSeeds.INVALID_CREATED_END_DEVICE_EVENTS,
                            MessageSeeds.EMPTY_LIST, END_DEVICE_EVENT_ITEM));
            com.elster.jupiter.metering.readings.EndDeviceEvent createdEndDeviceEvent = endDeviceBuilder.prepareCreateFrom(endDeviceEvent).build();
            context.commit();
            return createResponseMessage(createdEndDeviceEvent, HeaderType.Verb.CREATED, endDeviceEvents.size() > 1);
        } catch (VerboseConstraintViolationException e) {
            throw messageFactory.createEndDeviceEventsFaultMessage(MessageSeeds.INVALID_CREATED_END_DEVICE_EVENTS, e.getLocalizedMessage());
        } catch (LocalizedException e) {
            throw messageFactory.createEndDeviceEventsFaultMessage(MessageSeeds.INVALID_CREATED_END_DEVICE_EVENTS, e.getLocalizedMessage(), e.getErrorCode());
        }
    }

    @Override
    public EndDeviceEventsResponseMessageType closedEndDeviceEvents(EndDeviceEventsEventMessageType closedEndDeviceEventsEventMessage) throws FaultMessage {
        endPointHelper.setSecurityContext();
        try (TransactionContext context = transactionService.getContext()) {
            List<EndDeviceEvent> endDeviceEvents = retrieveEndDeviceEvents(closedEndDeviceEventsEventMessage.getPayload(), MessageSeeds.INVALID_CLOSED_END_DEVICE_EVENTS);
            EndDeviceEvent endDeviceEvent = endDeviceEvents.stream().findFirst()
                    .orElseThrow(messageFactory.createEndDeviceEventsFaultMessageSupplier(MessageSeeds.INVALID_CLOSED_END_DEVICE_EVENTS,
                            MessageSeeds.EMPTY_LIST, END_DEVICE_EVENT_ITEM));
            com.elster.jupiter.metering.readings.EndDeviceEvent closedEndDeviceEvent = endDeviceBuilder.prepareCloseFrom(endDeviceEvent).build();
            context.commit();
            return null;
        } catch (VerboseConstraintViolationException e) {
            throw messageFactory.createEndDeviceEventsFaultMessage(MessageSeeds.INVALID_CLOSED_END_DEVICE_EVENTS, e.getLocalizedMessage());
        } catch (LocalizedException e) {
            throw messageFactory.createEndDeviceEventsFaultMessage(MessageSeeds.INVALID_CLOSED_END_DEVICE_EVENTS, e.getLocalizedMessage(), e.getErrorCode());
        }
    }

    @Override
    public EndDeviceEventsResponseMessageType changedEndDeviceEvents(EndDeviceEventsEventMessageType changedEndDeviceEventsEventMessage) throws FaultMessage {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public EndDeviceEventsResponseMessageType canceledEndDeviceEvents(EndDeviceEventsEventMessageType canceledEndDeviceEventsEventMessage) throws FaultMessage {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public EndDeviceEventsResponseMessageType deletedEndDeviceEvents(EndDeviceEventsEventMessageType deletedEndDeviceEventsEventMessage) throws FaultMessage {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private EndDeviceEventsResponseMessageType createResponseMessage(com.elster.jupiter.metering.readings.EndDeviceEvent createdEndDeviceEvent, HeaderType.Verb verb, boolean bulk) {
        EndDeviceEventsResponseMessageType responseMessage = endDeviceEventsMessageObjectFactory.createEndDeviceEventsResponseMessageType();

        // set header
        HeaderType header = cimMessageObjectFactory.createHeaderType();
        header.setNoun(NOUN);
        header.setVerb(verb);
        responseMessage.setHeader(header);

        // set reply
        ReplyType reply = bulk ? replyTypeFactory.partialFailureReplyType(MessageSeeds.UNSUPPORTED_BULK_OPERATION, "EndDeviceEvents.EndDeviceEvent") : replyTypeFactory.okReplyType();
        responseMessage.setReply(reply);

        // set payload
        EndDeviceEventsPayloadType payload = endDeviceEventsMessageObjectFactory.createEndDeviceEventsPayloadType();
        EndDeviceEvents endDeviceEvents = endDeviceEventsFactory.asEndDeviceEvents(createdEndDeviceEvent);
        payload.setEndDeviceEvents(endDeviceEvents);
        responseMessage.setPayload(payload);

        return responseMessage;
    }

    private List<EndDeviceEvent> retrieveEndDeviceEvents(EndDeviceEventsPayloadType payload, MessageSeeds basicFaultMessage) throws ch.iec.tc57._2011.receiveenddeviceevents.FaultMessage {
        if (payload == null) {
            throw messageFactory.createEndDeviceEventsFaultMessageSupplier(basicFaultMessage, MessageSeeds.MISSING_ELEMENT, "Payload").get();
        }
        EndDeviceEvents endDeviceEvents = payload.getEndDeviceEvents();
        if (endDeviceEvents == null) {
            throw messageFactory.createEndDeviceEventsFaultMessageSupplier(basicFaultMessage, MessageSeeds.MISSING_ELEMENT, NOUN).get();
        }
        return endDeviceEvents.getEndDeviceEvent();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        ImmutableList.Builder<PropertySpec> builder = ImmutableList.builder();

        builder.add(propertySpecService
                .specForValuesOf(new ObisCodeValueFactory())
                .named("EndDeviceEvents.ObisCode", "Logbook OBIS code")
                .describedAs("Logbook OBIS code")
//                .named(TranslationKeys.MAX_PERIOD_OF_CONSECUTIVE_SUSPECTS)
//                .describedAs(TranslationKeys.MAX_PERIOD_OF_CONSECUTIVE_SUSPECTS_DESCRIPTION)
//                .fromThesaurus(thesaurus)
                .markRequired()
                .markEditable()
                .finish());

        return builder.build();
    }
}

