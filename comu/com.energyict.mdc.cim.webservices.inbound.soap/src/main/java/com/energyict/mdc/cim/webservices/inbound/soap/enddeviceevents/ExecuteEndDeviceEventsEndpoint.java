package com.energyict.mdc.cim.webservices.inbound.soap.enddeviceevents;

import com.elster.jupiter.domain.util.VerboseConstraintViolationException;
import com.elster.jupiter.nls.LocalizedException;
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
import com.google.common.collect.ImmutableList;

import javax.inject.Inject;
import java.util.List;

public class ExecuteEndDeviceEventsEndpoint implements EndDeviceEventsPort, EndPointProp {
    private static final String NOUN = "EndDeviceEvents";
    private static final String ALARM_SEVERITY = "Alarm";

    private final EndPointHelper endPointHelper;
    private final ReplyTypeFactory replyTypeFactory;
    private final EndDeviceEventsFaultMessageFactory messageFactory;
    private final TransactionService transactionService;
    private final EndDeviceEventsBuilder endDeviceBuilder;
    private final ch.iec.tc57._2011.schema.message.ObjectFactory cimMessageObjectFactory
            = new ch.iec.tc57._2011.schema.message.ObjectFactory();
    private final ch.iec.tc57._2011.enddeviceeventsmessage.ObjectFactory endDeviceEventsMessageObjectFactory
            = new ch.iec.tc57._2011.enddeviceeventsmessage.ObjectFactory();
//    private final MeterDataStoreCommand meterDataStoreCommand;

    private final PropertySpecService propertySpecService;

    @Inject
    ExecuteEndDeviceEventsEndpoint(EndPointHelper endPointHelper,
                                   ReplyTypeFactory replyTypeFactory,
                                   EndDeviceEventsFaultMessageFactory messageFactory,
                                   TransactionService transactionService,
                                   EndDeviceEventsBuilder endDeviceBuilder,
                                   PropertySpecService propertySpecService) {
        this.endPointHelper = endPointHelper;
        this.replyTypeFactory = replyTypeFactory;
        this.messageFactory = messageFactory;
        this.transactionService = transactionService;
        this.endDeviceBuilder = endDeviceBuilder;
        this.propertySpecService = propertySpecService;
    }

    @Override
    public EndDeviceEventsResponseMessageType createdEndDeviceEvents(EndDeviceEventsEventMessageType createdEndDeviceEventsEventMessage) throws FaultMessage {
        endPointHelper.setSecurityContext();
        try (TransactionContext context = transactionService.getContext()) {
            List<EndDeviceEvent> endDeviceEvents = retrieveEndDeviceEvents(createdEndDeviceEventsEventMessage.getPayload(), MessageSeeds.INVALID_CREATED_END_DEVICE_EVENTS);
            EndDeviceEvent endDeviceEvent = endDeviceEvents.stream().findFirst()
                    .orElseThrow(messageFactory.createEndDeviceEventsFaultMessageSupplier(MessageSeeds.INVALID_CREATED_END_DEVICE_EVENTS,
                            MessageSeeds.EMPTY_LIST, "EndDeviceEvents.EndDeviceEvent"));
            if (endDeviceEvent.getSeverity().equals(ALARM_SEVERITY)) {

            }
            context.commit();
            return null;
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
                            MessageSeeds.EMPTY_LIST, "EndDeviceEvents.EndDeviceEvent"));
            if (endDeviceEvent.getSeverity().equals(ALARM_SEVERITY)) {

            }
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

    private List<EndDeviceEvent> retrieveEndDeviceEvents(EndDeviceEventsPayloadType payload, MessageSeeds basicFaultMessage) throws ch.iec.tc57._2011.receiveenddeviceevents.FaultMessage {
        if (payload == null) {
            throw messageFactory.createEndDeviceEventsFaultMessageSupplier(basicFaultMessage, MessageSeeds.MISSING_ELEMENT, "Payload").get();
        }
        EndDeviceEvents endDeviceEvents = payload.getEndDeviceEvents();
        if (endDeviceEvents == null) {
            throw messageFactory.createEndDeviceEventsFaultMessageSupplier(basicFaultMessage, MessageSeeds.MISSING_ELEMENT, "EndDeviceEvents").get();
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
//                .fromThesaurus(thesaurus)
                .markRequired()
                .markEditable()
                .finish());

        return builder.build();
    }
}

