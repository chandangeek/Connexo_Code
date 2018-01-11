/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.getenddeviceevents;

import com.elster.jupiter.cbo.Status;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.XsdDateTimeConverter;

import ch.iec.tc57._2011.enddeviceevents.EndDeviceEvent;
import ch.iec.tc57._2011.enddeviceevents.EndDeviceEventDetail;
import ch.iec.tc57._2011.enddeviceevents.EndDeviceEvents;
import ch.iec.tc57._2011.enddeviceevents.ObjectFactory;
import ch.iec.tc57._2011.getenddeviceevents.DateTimeInterval;
import ch.iec.tc57._2011.getenddeviceevents.FaultMessage;
import ch.iec.tc57._2011.getenddeviceevents.Meter;
import ch.iec.tc57._2011.getenddeviceevents.Name;
import ch.iec.tc57._2011.getenddeviceevents.TimeSchedule;
import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class EndDeviceEventsBuilder {
    private final ObjectFactory payloadObjectFactory = new ObjectFactory();

    private final Clock clock;
    private final MeteringService meteringService;
    private final EndDeviceEventsFaultMessageFactory faultMessageFactory;

    private List<EndDevice> endDevices;
    private RangeSet<Instant> timePeriods;

    @Inject
    public EndDeviceEventsBuilder(MeteringService meteringService,
                                  EndDeviceEventsFaultMessageFactory faultMessageFactory,
                                  Clock clock) {
        this.meteringService = meteringService;
        this.faultMessageFactory = faultMessageFactory;
        this.clock = clock;
    }

    public EndDeviceEventsBuilder prepareGetFrom(List<Meter> meters, List<TimeSchedule> timeSchedules) throws FaultMessage {
        Set<String> mRIDs = new HashSet<>();
        Set<String> names = new HashSet<>();

        meters.stream().forEach(meter -> {
            Optional<String> mRID = extractMrid(meter);
            Optional<String> name = extractName(meter);
            if (mRID.isPresent()) {
                mRIDs.add(mRID.get());
            } else if (name.isPresent()) {
                names.add(name.get());
            }
        });

        if (mRIDs.isEmpty() && names.isEmpty()) {
            throw faultMessageFactory.createEndDeviceEventsFaultMessageSupplier(MessageSeeds.END_DEVICE_IDENTIFIER_MISSING).get();
        }

        endDevices = meteringService.findEndDevices(mRIDs, names).find();
        timePeriods = getTimeIntervals(timeSchedules);

        return this;
    }

    /*
     * Filtering by end device identifier - mRID or name.
     * If mRID is specified, find end device by mRid (name is skipped and is not validated).
     * If name is specified, find end device by name.
     * Otherwise, this meter tag is skipped.
     */

    private Optional<String> extractMrid(Meter meter) {
        return Optional.ofNullable(meter.getMRID()).filter(mrid -> !Checks.is(mrid).emptyOrOnlyWhiteSpace());
    }

    private Optional<String> extractName(Meter meter) {
        return meter.getNames().stream().map(Name::getName).filter(name -> !Checks.is(name).emptyOrOnlyWhiteSpace()).findFirst();
    }

    /*
     * Filtering by date (between start & end date).
     * Start date is mandatory.
     * If only the start date is given, the end date = now
     */

    private RangeSet<Instant> getTimeIntervals(List<TimeSchedule> timeSchedules) throws FaultMessage {
        RangeSet<Instant> result = TreeRangeSet.create();
        if (timeSchedules.isEmpty()) {
            result.addAll(ImmutableRangeSet.of(Range.all()));
        } else {
            // only the first period is taken into account
            result.add(getTimeInterval(timeSchedules.get(0)));
        }
        return result;
    }

    private Range<Instant> getTimeInterval(TimeSchedule timeSchedule) throws FaultMessage {
        DateTimeInterval interval = timeSchedule.getScheduleInterval();
        Instant start = interval.getStart();
        if (start == null) {
            throw faultMessageFactory.createEndDeviceEventsFaultMessageSupplier(MessageSeeds.MISSING_ELEMENT, "TimeSchedule.start").get();
        }
        Instant end = interval.getEnd();
        if (end == null) {
            end = clock.instant();
        }
        if (!end.isAfter(start)) {
            throw faultMessageFactory.createEndDeviceEventsFaultMessageSupplier(
                    MessageSeeds.INVALID_OR_EMPTY_TIME_PERIOD,
                    XsdDateTimeConverter.marshalDateTime(start),
                    XsdDateTimeConverter.marshalDateTime(end)).get();
        }
        return Range.openClosed(start, end);
    }

    public EndDeviceEvents build() throws FaultMessage {
        EndDeviceEvents endDeviceEvents = new EndDeviceEvents();
        List<EndDeviceEvent> endDeviceEventList = endDeviceEvents.getEndDeviceEvent();

        endDevices.stream().forEach(
                endDevice -> endDevice.getDeviceEvents(timePeriods.span()).stream().forEach(
                        deviceEvent -> {
                            EndDeviceEvent endDeviceEvent = payloadObjectFactory.createEndDeviceEvent();
                            endDeviceEvent.setEndDeviceEventType(toEndDeviceEventType(deviceEvent.getEventType()));
                            endDeviceEvent.setMRID(deviceEvent.getMRID());
                            endDeviceEvent.setCreatedDateTime(deviceEvent.getCreatedDateTime());
                            endDeviceEvent.setIssuerID(deviceEvent.getIssuerID());
                            endDeviceEvent.setIssuerTrackingID(deviceEvent.getIssuerTrackingID());
                            endDeviceEvent.setSeverity(deviceEvent.getSeverity());
                            endDeviceEvent.setStatus(toStatus(deviceEvent.getStatus()));

                            deviceEvent.getProperties().entrySet().stream().forEach(property -> {
                                        EndDeviceEventDetail endDeviceEventDetail = payloadObjectFactory.createEndDeviceEventDetail();
                                        endDeviceEventDetail.setName(property.getKey());
                                        endDeviceEventDetail.setValue(property.getValue());
                                        endDeviceEvent.getEndDeviceEventDetails().add(endDeviceEventDetail);
                                    }
                            );
                            endDeviceEventList.add(endDeviceEvent);
                        }));
        return endDeviceEvents;
    }

    private EndDeviceEvent.EndDeviceEventType toEndDeviceEventType(com.elster.jupiter.metering.events.EndDeviceEventType eventType) {
        EndDeviceEvent.EndDeviceEventType type = payloadObjectFactory.createEndDeviceEventEndDeviceEventType();
        type.setRef(eventType.getMRID());
        return type;
    }

    private ch.iec.tc57._2011.enddeviceevents.Status toStatus(Status status) {
        if (status == null) {
            return null;
        }
        ch.iec.tc57._2011.enddeviceevents.Status state = payloadObjectFactory.createStatus();
        state.setDateTime(status.getDateTime());
        state.setReason(status.getReason());
        state.setRemark(status.getRemark());
        state.setValue(status.getValue());
        return state;
    }
}
