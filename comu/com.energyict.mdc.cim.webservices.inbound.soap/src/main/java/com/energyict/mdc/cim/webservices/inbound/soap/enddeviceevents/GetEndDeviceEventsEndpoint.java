/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.enddeviceevents;

import com.elster.jupiter.domain.util.VerboseConstraintViolationException;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.EndPointHelper;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.ReplyTypeFactory;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.XsdDateTimeConverter;

import ch.iec.tc57._2011.enddeviceevents.EndDeviceEvents;
import ch.iec.tc57._2011.getenddeviceevents.DateTimeInterval;
import ch.iec.tc57._2011.getenddeviceevents.FaultMessage;
import ch.iec.tc57._2011.getenddeviceevents.GetEndDeviceEvents;
import ch.iec.tc57._2011.getenddeviceevents.GetEndDeviceEventsPort;
import ch.iec.tc57._2011.getenddeviceevents.Meter;
import ch.iec.tc57._2011.getenddeviceevents.Name;
import ch.iec.tc57._2011.getenddeviceevents.TimeSchedule;
import ch.iec.tc57._2011.getenddeviceeventsmessage.EndDeviceEventsPayloadType;
import ch.iec.tc57._2011.getenddeviceeventsmessage.EndDeviceEventsResponseMessageType;
import ch.iec.tc57._2011.getenddeviceeventsmessage.GetEndDeviceEventsRequestMessageType;
import ch.iec.tc57._2011.schema.message.HeaderType;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

import javax.inject.Inject;
import javax.inject.Provider;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class GetEndDeviceEventsEndpoint implements GetEndDeviceEventsPort {
    private static final String NOUN = "EndDeviceEvents";
    private static final String GET_END_DEVICE_EVENTS_ITEM = "GetEndDeviceEvents";

    private final EndPointHelper endPointHelper;
    private final ReplyTypeFactory replyTypeFactory;
    private final EndDeviceEventsFaultMessageFactory messageFactory;
    private final TransactionService transactionService;
    private final Provider<EndDeviceEventsBuilder> endDeviceBuilderProvider;
    private final ch.iec.tc57._2011.schema.message.ObjectFactory cimMessageObjectFactory
            = new ch.iec.tc57._2011.schema.message.ObjectFactory();
    private final ch.iec.tc57._2011.getenddeviceeventsmessage.ObjectFactory endDeviceEventsMessageObjectFactory
            = new ch.iec.tc57._2011.getenddeviceeventsmessage.ObjectFactory();

    private final Clock clock;

    @Inject
    GetEndDeviceEventsEndpoint(EndPointHelper endPointHelper,
                               ReplyTypeFactory replyTypeFactory,
                               EndDeviceEventsFaultMessageFactory messageFactory,
                               TransactionService transactionService,
                               Provider<EndDeviceEventsBuilder> endDeviceBuilderProvider,
                               Clock clock) {
        this.endPointHelper = endPointHelper;
        this.replyTypeFactory = replyTypeFactory;
        this.messageFactory = messageFactory;
        this.transactionService = transactionService;
        this.endDeviceBuilderProvider = endDeviceBuilderProvider;
        this.clock = clock;
    }

    @Override
    public EndDeviceEventsResponseMessageType getEndDeviceEvents(GetEndDeviceEventsRequestMessageType getEndDeviceEventsRequestMessage) throws FaultMessage {
        endPointHelper.setSecurityContext();
        try (TransactionContext context = transactionService.getContext()) {
            GetEndDeviceEvents getEndDeviceEvents = Optional.ofNullable(getEndDeviceEventsRequestMessage.getRequest().getGetEndDeviceEvents())
                    .orElseThrow(messageFactory.getEndDeviceEventsFaultMessageSupplier(MessageSeeds.MISSING_ELEMENT, GET_END_DEVICE_EVENTS_ITEM));
            if (!getEndDeviceEvents.getEndDeviceEvent().isEmpty()) {
                throw messageFactory.getEndDeviceEventsFaultMessageSupplier(MessageSeeds.UNSUPPORTED_ELEMENT, "EndDeviceEvent", GET_END_DEVICE_EVENTS_ITEM).get();
            }
            List<Meter> meters = getEndDeviceEvents.getMeter();
            // todo mlevan bulk?
            Meter meter = meters.stream().findFirst()
                    .orElseThrow(messageFactory.getEndDeviceEventsFaultMessageSupplier(MessageSeeds.EMPTY_LIST, "EndDeviceEvents.Meters"));
            EndDeviceEventsBuilder builder = endDeviceBuilderProvider.get();
            setMeterInfo(builder, meter);

            EndDeviceEvents result = builder
                    .inTimeIntervals(getTimeIntervals(getEndDeviceEvents.getTimeSchedule()))
                    .build();
            return createEndDeviceEventsResponseMessageType(result, meters.size() > 1);
        } catch (VerboseConstraintViolationException e) {
            throw messageFactory.getEndDeviceEventsFaultMessage(e.getLocalizedMessage());
        } catch (LocalizedException e) {
            throw messageFactory.getEndDeviceEventsFaultMessage(e.getLocalizedMessage(), e.getErrorCode());
        }
    }

    private void setMeterInfo(EndDeviceEventsBuilder builder, Meter meter) throws ch.iec.tc57._2011.getenddeviceevents.FaultMessage {
        String mRID = meter.getMRID();
        if (mRID == null) {
            Set<String> names = meter.getNames().stream()
                    .map(Name::getName).filter(Objects::nonNull).collect(Collectors.toSet());
            if (names.size() != 1) {
                throw messageFactory.getEndDeviceEventsFaultMessageSupplier(MessageSeeds.UNSUPPORTED_LIST_SIZE, "Meter.Names[name]", 1).get();
            }
            String name = names.stream()
                    .findFirst()
                    .orElseThrow(messageFactory.getEndDeviceEventsFaultMessageSupplier(MessageSeeds.MISSING_MRID_OR_NAME_WITH_TYPE_FOR_ELEMENT, "EndDevice", "Meter.Names.name"));
            if (Checks.is(name).emptyOrOnlyWhiteSpace()) {
                throw messageFactory.getEndDeviceEventsFaultMessageSupplier(MessageSeeds.EMPTY_ELEMENT, "Meter.Names.name").get();
            }
            builder.fromDeviceWithName(name);
        } else {
            if (Checks.is(mRID).emptyOrOnlyWhiteSpace()) {
                throw messageFactory.getEndDeviceEventsFaultMessageSupplier(MessageSeeds.EMPTY_ELEMENT, "Meter.Names.name").get();
            }
            builder.fromDeviceWithMRID(mRID);
        }
    }

    private RangeSet<Instant> getTimeIntervals(List<TimeSchedule> timeSchedules) throws ch.iec.tc57._2011.getenddeviceevents.FaultMessage {
        RangeSet<Instant> result = TreeRangeSet.create();
        for (int i = 0; i < timeSchedules.size(); ++i) {
            result.add(getTimeInterval(timeSchedules.get(i), i));
        }
        return result;
    }

    private Range<Instant> getTimeInterval(TimeSchedule timeSchedule, int index) throws ch.iec.tc57._2011.getenddeviceevents.FaultMessage {
        final String READING_ITEM = "TimeSchedule" + '[' + index + ']';
        DateTimeInterval interval = timeSchedule.getScheduleInterval();
        if (interval == null) {
            throw messageFactory.getEndDeviceEventsFaultMessageSupplier(MessageSeeds.MISSING_ELEMENT, READING_ITEM + ".timePeriod").get();
        }
        Instant start = interval.getStart();
        if (start == null) {
            throw messageFactory.getEndDeviceEventsFaultMessageSupplier(MessageSeeds.MISSING_ELEMENT, READING_ITEM + ".timePeriod.start").get();
        }
        Instant end = interval.getEnd();
        if (end == null) {
            end = clock.instant();
        }
        if (!end.isAfter(start)) {
            throw messageFactory.getEndDeviceEventsFaultMessageSupplier(
                    MessageSeeds.INVALID_OR_EMPTY_TIME_PERIOD,
                    XsdDateTimeConverter.marshalDateTime(start),
                    XsdDateTimeConverter.marshalDateTime(end)).get();
        }
        return Range.openClosed(start, end);
    }

    private EndDeviceEventsResponseMessageType createEndDeviceEventsResponseMessageType(EndDeviceEvents endDeviceEvents, boolean bulkRequested) {
        EndDeviceEventsResponseMessageType endDeviceEventsResponseMessageType
                = endDeviceEventsMessageObjectFactory.createEndDeviceEventsResponseMessageType();
        HeaderType header = cimMessageObjectFactory.createHeaderType();
        header.setVerb(HeaderType.Verb.REPLY);
        header.setNoun(NOUN);
        endDeviceEventsResponseMessageType.setHeader(header);
        endDeviceEventsResponseMessageType.setReply(bulkRequested ?
                replyTypeFactory.partialFailureReplyType(MessageSeeds.UNSUPPORTED_BULK_OPERATION, "") :
                replyTypeFactory.okReplyType());
        EndDeviceEventsPayloadType endDeviceEventsPayloadType =
                endDeviceEventsMessageObjectFactory.createEndDeviceEventsPayloadType();
        endDeviceEventsPayloadType.setEndDeviceEvents(endDeviceEvents);
        endDeviceEventsResponseMessageType.setPayload(endDeviceEventsPayloadType);
        return endDeviceEventsResponseMessageType;
    }
}

