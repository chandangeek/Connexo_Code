/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.getenddeviceevents;

import com.elster.jupiter.cbo.Status;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.streams.Functions;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.XsdDateTimeConverter;

import ch.iec.tc57._2011.enddeviceevents.Asset;
import ch.iec.tc57._2011.enddeviceevents.EndDeviceEvent;
import ch.iec.tc57._2011.enddeviceevents.EndDeviceEventDetail;
import ch.iec.tc57._2011.enddeviceevents.EndDeviceEvents;
import ch.iec.tc57._2011.enddeviceevents.NameType;
import ch.iec.tc57._2011.enddeviceevents.ObjectFactory;
import ch.iec.tc57._2011.getenddeviceevents.DateTimeInterval;
import ch.iec.tc57._2011.getenddeviceevents.EndDeviceEventType;
import ch.iec.tc57._2011.getenddeviceevents.EndDeviceGroup;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class EndDeviceEventsBuilder {
    private static final String END_DEVICE_NAME_TYPE = "EndDevice";
    public static final String DEVICE_PROTOCOL_CODE_LABEL = "DeviceProtocolCode";

    private final ObjectFactory payloadObjectFactory = new ObjectFactory();

    private final Clock clock;
    private final MeteringService meteringService;
    private final EndDeviceEventsFaultMessageFactory faultMessageFactory;

    private List<EndDevice> endDevices;
    private Range<Instant> timePeriods;
    private Set<int[]> eventTypeFilters;

    @Inject
    public EndDeviceEventsBuilder(MeteringService meteringService,
                                  EndDeviceEventsFaultMessageFactory faultMessageFactory,
                                  Clock clock) {
        this.meteringService = meteringService;
        this.faultMessageFactory = faultMessageFactory;
        this.clock = clock;
    }

    public EndDeviceEventsBuilder prepareGetFrom(List<Meter> meters, List<TimeSchedule> timeSchedules) throws FaultMessage {
        Set<String> deviceIdentifiers = getMeterIdentifiers(meters);
        if (deviceIdentifiers.isEmpty()) {
            throw faultMessageFactory.createEndDeviceEventsFaultMessageSupplier(MessageSeeds.END_DEVICE_IDENTIFIER_MISSING).get();
        }
        endDevices = meteringService.findEndDevices(deviceIdentifiers);
        timePeriods = getTimeIntervals(timeSchedules);
        return this;
    }

    public EndDeviceEventsBuilder setEndDeviceEventTypeFilters(List<EndDeviceEventType> eventTypes) {
        eventTypeFilters = getEventTypeFilters(getEventTypeIdentifiers(eventTypes));
        return this;
    }

    public EndDeviceEvents build() throws FaultMessage {
        EndDeviceEvents endDeviceEvents = new EndDeviceEvents();
        List<EndDeviceEvent> endDeviceEventList = endDeviceEvents.getEndDeviceEvent();
        endDevices.forEach(endDevice -> {
            if (eventTypeFilters.isEmpty()) {
                endDevice.getDeviceEvents(timePeriods).forEach(deviceEvent -> endDeviceEventList.add(asEndDeviceEvent(deviceEvent)));
            } else {
                endDevice.getDeviceEvents(timePeriods).stream().filter(filter(eventTypeFilters)).forEach(
                        deviceEvent -> endDeviceEventList.add(asEndDeviceEvent(deviceEvent)));
            }
        });
        return endDeviceEvents;
    }

    /*
     * Filtering by end device identifier - mRID or name or serial number.
     * If mRID is specified, find end device by mRid (name and serial number is skipped and is not validated).
     * If serial number is specified, find end device by serial number.
     * If name is specified, find end device by name.
     * Otherwise, this meter tag is skipped.
     */
    public Set<String> getMeterIdentifiers(List<Meter> meters) {
        Set<String> identifiers = new HashSet<>();
        meters.forEach(meter -> {
            Optional<String> mRID = extractMrid(meter);
            Optional<String> serialNumber = extractName(meter);
            Optional<String> name = extractName(meter);
            if (mRID.isPresent()) {
                identifiers.add(mRID.get());
            } else if (serialNumber.isPresent()) {
                identifiers.add(serialNumber.get());
            } else if (name.isPresent()) {
                identifiers.add(name.get());
            }
        });
        return identifiers;
    }

    /*
     * Filtering by end device group name.
     * If name is specified, find device group by name.
     * Otherwise, this device group tag is skipped.
     */
    public Set<String> getDeviceGroupIdentifiers(List<EndDeviceGroup> deviceGroups) {
        return deviceGroups.stream().map(EndDeviceEventsBuilder::extractName).flatMap(Functions.asStream()).collect(Collectors.toSet());
    }

    public void checkDeviceIdentifiersPresentOrThrowException(Set<String> meters, Set<String> deviceGroups) throws FaultMessage {
        if (meters.isEmpty() && deviceGroups.isEmpty()) {
            throw faultMessageFactory.createEndDeviceEventsFaultMessageSupplier(MessageSeeds.DEVICE_OR_GROUP_IDENTIFIER_MISSING).get();
        }
    }

    /*
     * Filtering by end event type.
     * If name is specified, find events of the specified type.
     * Otherwise, all event types are shown.
     */
    public Set<String> getEventTypeIdentifiers(List<EndDeviceEventType> eventTypes) {
        return eventTypes.stream().map(EndDeviceEventsBuilder::extractName).flatMap(Functions.asStream()).collect(Collectors.toSet());
    }

    /*
     * Filtering by date (between start & end date).
     * Start date is mandatory.
     * If only the start date is given, the end date = now
     */
    public Range<Instant> getTimeIntervals(List<TimeSchedule> timeSchedules) throws FaultMessage {
        RangeSet<Instant> result = TreeRangeSet.create();
        if (timeSchedules.isEmpty()) {
            result.addAll(ImmutableRangeSet.of(Range.all()));
        } else {
            // only the first period is taken into account
            result.add(getTimeInterval(timeSchedules.get(0)));
        }
        return result.span();
    }

    public EndDeviceEvent asEndDeviceEvent(EndDeviceEventRecord record) {
        EndDeviceEvent endDeviceEvent = payloadObjectFactory.createEndDeviceEvent();
        endDeviceEvent.setEndDeviceEventType(toEndDeviceEventType(record.getEventType()));
        endDeviceEvent.setMRID(record.getMRID());
        endDeviceEvent.setCreatedDateTime(record.getCreatedDateTime());
        endDeviceEvent.setIssuerID(record.getIssuerID());
        endDeviceEvent.setIssuerTrackingID(record.getIssuerTrackingID());
        endDeviceEvent.setSeverity(record.getSeverity());
        endDeviceEvent.setStatus(toStatus(record.getStatus()));
        endDeviceEvent.setReason(record.getDescription());
        endDeviceEvent.setAssets(createAsset(record.getEndDevice()));
        record.getProperties().entrySet().stream().forEach(property -> {
                    EndDeviceEventDetail endDeviceEventDetail = payloadObjectFactory.createEndDeviceEventDetail();
                    endDeviceEventDetail.setName(property.getKey());
                    endDeviceEventDetail.setValue(property.getValue());
                    endDeviceEvent.getEndDeviceEventDetails().add(endDeviceEventDetail);
                }
        );
        Optional<EndDeviceEventDetail> optionalOfEndDeviceEventDetail = setDeviceEventTypeDetail(record);
        if (optionalOfEndDeviceEventDetail.isPresent()) {
            endDeviceEvent.getEndDeviceEventDetails().add(optionalOfEndDeviceEventDetail.get());
        }
        return endDeviceEvent;
    }

    private Optional<EndDeviceEventDetail> setDeviceEventTypeDetail(EndDeviceEventRecord record) {
        String deviceEventType = record.getDeviceEventType();
        if (Objects.nonNull(deviceEventType) && !deviceEventType.isEmpty()) {
            EndDeviceEventDetail endDeviceEventDetail = new EndDeviceEventDetail();
            endDeviceEventDetail.setName(DEVICE_PROTOCOL_CODE_LABEL);
            endDeviceEventDetail.setValue(deviceEventType);
            return Optional.of(endDeviceEventDetail);
        }
        return Optional.empty();
    }

    private static Optional<String> extractMrid(Meter meter) {
        return Optional.ofNullable(meter.getMRID()).filter(mrid -> !Checks.is(mrid).emptyOrOnlyWhiteSpace());
    }

    private static Optional<String> extractName(Meter meter) {
        return meter.getNames().stream().map(Name::getName).filter(name -> !Checks.is(name).emptyOrOnlyWhiteSpace()).findFirst();
    }

    private static Optional<String> extractName(EndDeviceGroup deviceGroup) {
        return deviceGroup.getNames().stream().map(Name::getName).filter(name -> !Checks.is(name).emptyOrOnlyWhiteSpace()).findFirst();
    }

    private static Optional<String> extractName(EndDeviceEventType eventType) {
        return eventType.getNames().stream().map(Name::getName).filter(name -> {
            if (Checks.is(name).emptyOrOnlyWhiteSpace()) {
                return false;
            }
            if (name.trim().equals("*")) {
                return true;
            }
            List<String> parts = Arrays.asList(name.split("\\."));
            if (parts.size() != com.elster.jupiter.metering.events.EndDeviceEventType.MRID_FIELD_COUNT) {
                return false;
            }
            try {
                parts.stream().map(String::trim).filter(part -> !"*".equals(part)).forEach(Integer::parseInt);
            } catch (NumberFormatException ex) {
                return false;
            }
            return true;
        }).findFirst();
    }

    public static Set<int[]> getEventTypeFilters(Set<String> eventTypes) {
        Set<int[]> eventTypeFilters = new HashSet<>();
        if (eventTypes.isEmpty() || eventTypes.stream().anyMatch(str -> "*".equals(str.trim()))) {
            return eventTypeFilters;
        }
        for (String typeString : eventTypes) {
            String[] eventTypesToFilter = typeString.split("\\.");
            int[] eventTypeParts = new int[eventTypesToFilter.length];
            for (int count = 0; count < eventTypesToFilter.length; count++) {
                String part = eventTypesToFilter[count].trim();
                eventTypeParts[count] = part.equals("*") ? -1 : Integer.parseInt(part);
            }
            eventTypeFilters.add(eventTypeParts);
        }
        return eventTypeFilters;
    }

    public static Predicate<EndDeviceEventRecord> filter(Set<int[]> eventTypes) {
        return (record) -> {
            com.elster.jupiter.metering.events.EndDeviceEventType endDeviceEventType = record.getEventType();
            for (int[] list : eventTypes) {
                if (compareEventParts(list[com.elster.jupiter.metering.events.EndDeviceEventType.TYPE_INDEX], endDeviceEventType.getType().getValue())
                        && compareEventParts(list[com.elster.jupiter.metering.events.EndDeviceEventType.DOMAIN_INDEX], endDeviceEventType.getDomain().getValue())
                        && compareEventParts(list[com.elster.jupiter.metering.events.EndDeviceEventType.SUBDOMAIN_INDEX], endDeviceEventType.getSubDomain().getValue())
                        && compareEventParts(list[com.elster.jupiter.metering.events.EndDeviceEventType.EVENT_OR_ACTION_INDEX], endDeviceEventType.getEventOrAction().getValue())) {
                    return true;
                }
            }
            return false;
        };
    }

    private static boolean compareEventParts(Integer filter, int value) {
        return filter < 0 || filter.equals(value);
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

    private Asset createAsset(EndDevice endDevice) {
        Asset asset = payloadObjectFactory.createAsset();
        asset.setMRID(endDevice.getMRID());
        asset.getNames().add(createName(endDevice));
        return asset;
    }

    private ch.iec.tc57._2011.enddeviceevents.Name createName(EndDevice endDevice) {
        NameType nameType = payloadObjectFactory.createNameType();
        nameType.setName(END_DEVICE_NAME_TYPE);
        ch.iec.tc57._2011.enddeviceevents.Name name = payloadObjectFactory.createName();
        name.setNameType(nameType);
        name.setName(endDevice.getName());
        return name;
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
}