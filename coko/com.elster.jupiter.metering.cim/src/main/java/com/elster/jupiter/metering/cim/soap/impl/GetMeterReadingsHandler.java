package com.elster.jupiter.metering.cim.soap.impl;

import ch.iec.tc57._2011.getmeterreadings.EndDevice;
import ch.iec.tc57._2011.getmeterreadings.FaultMessage;
import ch.iec.tc57._2011.getmeterreadings.GetMeterReadings;
import ch.iec.tc57._2011.getmeterreadings.GetMeterReadingsPort;
import ch.iec.tc57._2011.getmeterreadings.ReadingType;
import ch.iec.tc57._2011.getmeterreadings.UsagePoint;
import ch.iec.tc57._2011.getmeterreadings.UsagePointGroup;
import ch.iec.tc57._2011.getmeterreadingsmessage.GetMeterReadingsRequestType;
import ch.iec.tc57._2011.getmeterreadingsmessage.MeterReadingsPayloadType;
import ch.iec.tc57._2011.getmeterreadingsmessage.ObjectFactory;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.schema.message.ReplyType;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.cim.impl.EnumeratedReadingTypeFilter;
import com.elster.jupiter.metering.cim.impl.MeterReadingsGenerator;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EndDeviceMembership;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.UsagePointMembership;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;

import javax.jws.WebParam;
import javax.xml.ws.Holder;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class GetMeterReadingsHandler implements GetMeterReadingsPort {
    private final ObjectFactory objectFactory = new ObjectFactory();
    private final MeteringService meteringService;
    private final MeteringGroupsService meteringGroupsService;
    private final ch.iec.tc57._2011.meterreadings.ObjectFactory payloadObjectFactory = new ch.iec.tc57._2011.meterreadings.ObjectFactory();
    private MeterReadingsGenerator meterReadingsGenerator = new MeterReadingsGenerator();

    public GetMeterReadingsHandler(MeteringService meteringService, MeteringGroupsService meteringGroupsService) {
        this.meteringService = meteringService;
        this.meteringGroupsService = meteringGroupsService;
    }

    @Override
    public void getMeterReadings(
            @WebParam(name = "Header", targetNamespace = "http://iec.ch/TC57/2011/GetMeterReadingsMessage", mode = WebParam.Mode.INOUT)
            Holder<HeaderType> header,
            @WebParam(name = "Request", targetNamespace = "http://iec.ch/TC57/2011/GetMeterReadingsMessage")
            GetMeterReadingsRequestType request,
            @WebParam(name = "Payload", targetNamespace = "http://iec.ch/TC57/2011/GetMeterReadingsMessage", mode = WebParam.Mode.INOUT)
            Holder<MeterReadingsPayloadType> payload,
            @WebParam(name = "Reply", targetNamespace = "http://iec.ch/TC57/2011/GetMeterReadingsMessage", mode = WebParam.Mode.OUT)
            Holder<ReplyType> reply
    ) throws FaultMessage {
        if (request != null) {
            Set<com.elster.jupiter.metering.ReadingType> requestedReadingTypes = requestedReadingTypes(request);
            if (!requestedReadingTypes.isEmpty()) {
                meterReadingsGenerator = new MeterReadingsGenerator(EnumeratedReadingTypeFilter.only(requestedReadingTypes));
            }

            MeterReadingsPayloadType meterReadingsPayloadType = objectFactory.createMeterReadingsPayloadType();
            payload.value = meterReadingsPayloadType;
            meterReadingsPayloadType.setMeterReadings(payloadObjectFactory.createMeterReadings());
            Range<Instant> range = getRange(request);

            addForRequestedUsagePoints(request, meterReadingsPayloadType, range);
            addForRequestedEndDevices(request, meterReadingsPayloadType, range);
            addForRequestedUsagePointGroups(request, meterReadingsPayloadType, range);
            addForRequestedEndDeviceGroups(request, meterReadingsPayloadType, range);
        }
    }

    private Set<com.elster.jupiter.metering.ReadingType> requestedReadingTypes(GetMeterReadingsRequestType request) {
        Set<com.elster.jupiter.metering.ReadingType> set = Collections.emptySet();
        if (request.getGetMeterReadings() != null) {
            ImmutableSet.Builder<com.elster.jupiter.metering.ReadingType> builder = ImmutableSet.builder();
            for (ReadingType readingType : request.getGetMeterReadings().getReadingType()) {
                String mrid = readingType.getMRID();
                Optional<com.elster.jupiter.metering.ReadingType> found = meteringService.getReadingType(mrid);
                if (found.isPresent()) {
                    builder.add(found.get());
                }
            }
            set = builder.build();
        }
        return set;
    }

    private void addForEndDevice(MeterReadingsPayloadType meterReadingsPayloadType, com.elster.jupiter.metering.EndDevice endDevice, Range<Instant> range) {
        if (endDevice instanceof Meter) {
            addForMeter(meterReadingsPayloadType, (Meter) endDevice, range);
        } else {
            meterReadingsGenerator.addEndDeviceEvents(meterReadingsPayloadType.getMeterReadings(), endDevice, range);
        }
    }

    private void addForEndDeviceGroup(MeterReadingsPayloadType meterReadingsPayloadType, com.elster.jupiter.metering.groups.EndDeviceGroup endDeviceGroup, Range<Instant> range) {
        for (EndDeviceMembership membership : endDeviceGroup.getMembers(range)) {
            addForMembership(meterReadingsPayloadType, membership);
        }
    }

    private void addForMembership(MeterReadingsPayloadType meterReadingsPayloadType, UsagePointMembership membership) {
        for (Range<Instant> subRange : membership.getRanges().asRanges()) {
            addForUsagePoint(meterReadingsPayloadType, membership.getUsagePoint(), subRange);
        }
    }

    private void addForMembership(MeterReadingsPayloadType meterReadingsPayloadType, EndDeviceMembership membership) {
        for (Range<Instant> subRange : membership.getRanges().asRanges()) {
            addForEndDevice(meterReadingsPayloadType, membership.getEndDevice(), subRange);
        }
    }

    private void addForMeter(MeterReadingsPayloadType meterReadingsPayloadType, Meter meter, Range<Instant> range) {
        Set<Map.Entry<Range<Instant>,MeterActivation>> entries = getMeterActivationsPerInterval(meter, range).entrySet();
        for (Map.Entry<Range<Instant>, MeterActivation> entry : entries) {
            meterReadingsGenerator.addMeterReadings(meterReadingsPayloadType.getMeterReadings(), entry.getValue(), entry.getKey());
        }
    }

    private void addForRequestedEndDeviceGroups(GetMeterReadingsRequestType request, MeterReadingsPayloadType meterReadingsPayloadType, Range<Instant> range) {
        for (String endDeviceGroupMrid : requestedEndDeviceGroups(request)) {
            Optional<EndDeviceGroup> found = meteringGroupsService.findEndDeviceGroup(endDeviceGroupMrid);
            if (found.isPresent()) {
                addForEndDeviceGroup(meterReadingsPayloadType, found.get(), range);
            }
        }
    }

    private void addForRequestedEndDevices(GetMeterReadingsRequestType request, MeterReadingsPayloadType meterReadingsPayloadType, Range<Instant> range) {
        for (String endDeviceMrid : requestedEndDevices(request)) {
            Optional<com.elster.jupiter.metering.EndDevice> found = meteringService.findEndDevice(endDeviceMrid);
            if (found.isPresent() && found.get() instanceof Meter) {
                addForMeter(meterReadingsPayloadType, (Meter) found.get(), range);
            }
        }
    }

    private void addForRequestedUsagePointGroups(GetMeterReadingsRequestType request, MeterReadingsPayloadType meterReadingsPayloadType, Range<Instant> range) {
        for (String usagePointGroupMrID : requestedUsagePointGroups(request)) {
            Optional<com.elster.jupiter.metering.groups.UsagePointGroup> found = meteringGroupsService.findUsagePointGroup(usagePointGroupMrID);
            if (found.isPresent()) {
                addForUsagePointGroup(meterReadingsPayloadType, found.get(), range);
            }
        }
    }

    private void addForRequestedUsagePoints(GetMeterReadingsRequestType request, MeterReadingsPayloadType meterReadingsPayloadType, Range<Instant> range) {
        List<String> usagePointMrids = requestedUsagePoints(request);
        for (String usagePointMrid : usagePointMrids) {
            Optional<com.elster.jupiter.metering.UsagePoint> found = meteringService.findUsagePoint(usagePointMrid);
            if (found.isPresent()) {
                addForUsagePoint(meterReadingsPayloadType, found.get(), range);
            }
        }
    }

    private void addForUsagePoint(MeterReadingsPayloadType meterReadingsPayloadType, com.elster.jupiter.metering.UsagePoint usagePoint, Range<Instant> range) {
        Set<Map.Entry<Range<Instant>,MeterActivation>> entries = getMeterActivationsPerInterval(usagePoint, range).entrySet();
        for (Map.Entry<Range<Instant>, MeterActivation> entry : entries) {
            meterReadingsGenerator.addMeterReadings(meterReadingsPayloadType.getMeterReadings(), entry.getValue(), entry.getKey());
        }
    }

    private void addForUsagePointGroup(MeterReadingsPayloadType meterReadingsPayloadType, com.elster.jupiter.metering.groups.UsagePointGroup usagePointGroup, Range<Instant> range) {
        List<UsagePointMembership> memberships = usagePointGroup.getMembers(range);
        for (UsagePointMembership membership : memberships) {
            addForMembership(meterReadingsPayloadType, membership);
        }
    }

    private Range<Instant> getRange(GetMeterReadingsRequestType request) {
        Date startTime = request.getStartTime();
        Date endTime = request.getEndTime();
        if (startTime == null) {
        	if (endTime == null) {
        		return Range.all();
        	} else {
        		return Range.atMost(endTime.toInstant());
        	}
        } else {
        	if (endTime == null) {
        		return Range.atLeast(startTime.toInstant());
        	} else {
        		return Range.closed(startTime.toInstant(), endTime.toInstant());
        	}
        }
    }

    private Map<Range<Instant>, MeterActivation> getMeterActivationsPerInterval(com.elster.jupiter.metering.UsagePoint usagePoint, Range<Instant> range) {
        Map<Range<Instant>, MeterActivation> map = new HashMap<>();
        for (MeterActivation meterActivation : usagePoint.getMeterActivations()) {
            if (meterActivation.overlaps(range)) {
                map.put(intersection(meterActivation, range).get(), meterActivation);
            }
        }
        return map;
    }

    private Map<Range<Instant>, MeterActivation> getMeterActivationsPerInterval(Meter meter, Range<Instant> range) {
        Map<Range<Instant>, MeterActivation> map = new HashMap<>();
        for (MeterActivation meterActivation : meter.getMeterActivations()) {
            if (meterActivation.overlaps(range)) {
                map.put(intersection(meterActivation, range).get(), meterActivation);
            }
        }
        return map;
    }

    private Optional<Range<Instant>> intersection(MeterActivation meterActivation, Range<Instant> range) {
    	return meterActivation.intersection(range);
    }

    private List<String> requestedEndDeviceGroups(GetMeterReadingsRequestType request) {
        List<String> endDeviceGroups = new ArrayList<>();
        if (request.getGetMeterReadings() != null) {
            GetMeterReadings getMeterReadings = request.getGetMeterReadings();
            for (ch.iec.tc57._2011.getmeterreadings.EndDeviceGroup endDeviceGroup : getMeterReadings.getEndDeviceGroup()) {
                endDeviceGroups.add(endDeviceGroup.getMRID());
            }
        }
        return endDeviceGroups;
    }

    private List<String> requestedEndDevices(GetMeterReadingsRequestType request) {
        List<String> endDeviceMrids = new ArrayList<>();
        if (request.getGetMeterReadings() != null) {
            GetMeterReadings getMeterReadings = request.getGetMeterReadings();
            for (EndDevice endDevice : getMeterReadings.getEndDevice()) {
                endDeviceMrids.add(endDevice.getMRID());
            }
        }
        return endDeviceMrids;
    }

    private List<String> requestedUsagePointGroups(GetMeterReadingsRequestType request) {
        List<String> usagePointGroups = new ArrayList<>();
        if (request.getGetMeterReadings() != null) {
            GetMeterReadings getMeterReadings = request.getGetMeterReadings();
            for (UsagePointGroup usagePointGroup : getMeterReadings.getUsagePointGroup()) {
                usagePointGroups.add(usagePointGroup.getMRID());
            }
        }
        return usagePointGroups;
    }

    private List<String> requestedUsagePoints(GetMeterReadingsRequestType request) {
        List<String> usagePointMrids = new ArrayList<>();
        if (request.getGetMeterReadings() != null) {
            GetMeterReadings getMeterReadings = request.getGetMeterReadings();
            for (UsagePoint usagePoint : getMeterReadings.getUsagePoint()) {
                usagePointMrids.add(usagePoint.getMRID());
            }
        }
        return usagePointMrids;
    }
}
