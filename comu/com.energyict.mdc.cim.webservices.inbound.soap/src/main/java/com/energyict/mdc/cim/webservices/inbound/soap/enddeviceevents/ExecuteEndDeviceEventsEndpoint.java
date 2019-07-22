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
import com.energyict.mdc.cim.webservices.inbound.soap.impl.TranslationKeys;
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
    private static final String PAYLOAD_ITEM = "Payload";

    private final EndPointHelper endPointHelper;
    private final ReplyTypeFactory replyTypeFactory;
    private final EndDeviceEventsFaultMessageFactory messageFactory;
    private final TransactionService transactionService;
    private final EndDeviceEventsBuilder endDeviceBuilder;

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
                                   PropertySpecService propertySpecService,
                                   Thesaurus thesaurus) {
        this.endPointHelper = endPointHelper;
        this.replyTypeFactory = replyTypeFactory;
        this.messageFactory = messageFactory;
        this.transactionService = transactionService;
        this.endDeviceBuilder = endDeviceBuilder;
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    @Override
    public EndDeviceEventsResponseMessageType createdEndDeviceEvents(EndDeviceEventsEventMessageType createdEndDeviceEventsEventMessage) throws FaultMessage {
        endPointHelper.setSecurityContext();
        try (TransactionContext context = transactionService.getContext()) {
            List<EndDeviceEvent> endDeviceEvents = getEndDeviceEvents(createdEndDeviceEventsEventMessage.getPayload(), MessageSeeds.INVALID_CREATED_END_DEVICE_EVENTS);
            EndDeviceEvent endDeviceEvent = endDeviceEvents.stream().findFirst()
                    .orElseThrow(messageFactory.endDeviceEventsFaultMessageSupplier(MessageSeeds.INVALID_CREATED_END_DEVICE_EVENTS,
                            MessageSeeds.EMPTY_LIST, END_DEVICE_EVENT_ITEM));
            EndDeviceEvents createdEndDeviceEvents = endDeviceBuilder.prepareCreateFrom(endDeviceEvent).build();
            context.commit();
            return createResponseMessage(createdEndDeviceEvents, HeaderType.Verb.CREATED, endDeviceEvents.size() > 1, createdEndDeviceEventsEventMessage.getHeader().getCorrelationID());
        } catch (VerboseConstraintViolationException e) {
            throw messageFactory.endDeviceEventsFaultMessage(MessageSeeds.INVALID_CREATED_END_DEVICE_EVENTS, e.getLocalizedMessage());
        } catch (LocalizedException e) {
            throw messageFactory.endDeviceEventsFaultMessage(MessageSeeds.INVALID_CREATED_END_DEVICE_EVENTS, e.getLocalizedMessage(), e.getErrorCode());
        }
    }

    @Override
    public EndDeviceEventsResponseMessageType closedEndDeviceEvents(EndDeviceEventsEventMessageType closedEndDeviceEventsEventMessage) throws FaultMessage {
        endPointHelper.setSecurityContext();
        try (TransactionContext context = transactionService.getContext()) {
            List<EndDeviceEvent> endDeviceEvents = getEndDeviceEvents(closedEndDeviceEventsEventMessage.getPayload(), MessageSeeds.INVALID_CLOSED_END_DEVICE_EVENTS);
            EndDeviceEvent endDeviceEvent = endDeviceEvents.stream().findFirst()
                    .orElseThrow(messageFactory.endDeviceEventsFaultMessageSupplier(MessageSeeds.INVALID_CLOSED_END_DEVICE_EVENTS,
                            MessageSeeds.EMPTY_LIST, END_DEVICE_EVENT_ITEM));
            EndDeviceEvents closedEndDeviceEvents = endDeviceBuilder.prepareCloseFrom(endDeviceEvent).build();
            context.commit();
            return createResponseMessage(closedEndDeviceEvents, HeaderType.Verb.CLOSED, endDeviceEvents.size() > 1, closedEndDeviceEventsEventMessage.getHeader().getCorrelationID());
        } catch (VerboseConstraintViolationException e) {
            throw messageFactory.endDeviceEventsFaultMessage(MessageSeeds.INVALID_CLOSED_END_DEVICE_EVENTS, e.getLocalizedMessage());
        } catch (LocalizedException e) {
            throw messageFactory.endDeviceEventsFaultMessage(MessageSeeds.INVALID_CLOSED_END_DEVICE_EVENTS, e.getLocalizedMessage(), e.getErrorCode());
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

    private EndDeviceEventsResponseMessageType createResponseMessage(EndDeviceEvents endDeviceEvents, HeaderType.Verb verb, boolean bulkRequired, String correlationId) {
        EndDeviceEventsResponseMessageType responseMessage = endDeviceEventsMessageObjectFactory.createEndDeviceEventsResponseMessageType();

        // set header
        HeaderType header = cimMessageObjectFactory.createHeaderType();
        header.setNoun(NOUN);
        header.setVerb(verb);
        if(correlationId != null) {
            header.setCorrelationID(correlationId);
        }
        responseMessage.setHeader(header);

        // set reply
        ReplyType reply = bulkRequired ?
                replyTypeFactory.partialFailureReplyType(MessageSeeds.UNSUPPORTED_BULK_OPERATION, END_DEVICE_EVENT_ITEM) :
                replyTypeFactory.okReplyType();
        responseMessage.setReply(reply);

        // set payload
        EndDeviceEventsPayloadType payload = endDeviceEventsMessageObjectFactory.createEndDeviceEventsPayloadType();
        payload.setEndDeviceEvents(endDeviceEvents);
        responseMessage.setPayload(payload);

        return responseMessage;
    }

    private List<EndDeviceEvent> getEndDeviceEvents(EndDeviceEventsPayloadType payload, MessageSeeds basicFaultMessage) throws FaultMessage {
        if (payload == null) {
            throw messageFactory.endDeviceEventsFaultMessageSupplier(basicFaultMessage, MessageSeeds.MISSING_ELEMENT, PAYLOAD_ITEM).get();
        }
        EndDeviceEvents endDeviceEvents = payload.getEndDeviceEvents();
        if (endDeviceEvents == null) {
            throw messageFactory.endDeviceEventsFaultMessageSupplier(basicFaultMessage, MessageSeeds.MISSING_ELEMENT, NOUN).get();
        }
        return endDeviceEvents.getEndDeviceEvent();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        ImmutableList.Builder<PropertySpec> builder = ImmutableList.builder();

        builder.add(propertySpecService
                .specForValuesOf(new ObisCodeValueFactory())
                .named(TranslationKeys.LOGBOOK_OBIS_CODE)
                .describedAs(TranslationKeys.LOGBOOK_OBIS_CODE)
                .fromThesaurus(thesaurus)
                .markRequired()
                .markEditable()
                .finish());

        return builder.build();
    }
}

