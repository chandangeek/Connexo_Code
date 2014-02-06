package com.elster.jupiter.metering.cim.soap.impl;

import ch.iec.tc57._2011.getmeterreadings.FaultMessage;
import ch.iec.tc57._2011.getmeterreadings.GetMeterReadingsPort;
import ch.iec.tc57._2011.getmeterreadings_.EndDevice;
import ch.iec.tc57._2011.getmeterreadings_.EndDeviceGroup;
import ch.iec.tc57._2011.getmeterreadings_.GetMeterReadings;
import ch.iec.tc57._2011.getmeterreadings_.UsagePoint;
import ch.iec.tc57._2011.getmeterreadings_.UsagePointGroup;
import ch.iec.tc57._2011.getmeterreadingsmessage.GetMeterReadingsRequestType;
import ch.iec.tc57._2011.getmeterreadingsmessage.MeterReadingsPayloadType;
import ch.iec.tc57._2011.getmeterreadingsmessage.ObjectFactory;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.schema.message.ReplyType;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.EndDeviceMembership;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.UsagePointMembership;
import com.elster.jupiter.metering.cim.impl.MeterReadingsGenerator;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;
import com.google.common.base.Optional;

import javax.jws.WebParam;
import javax.xml.ws.Holder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

class GetMeterReadingsPortImpl implements GetMeterReadingsPort {
    private final ObjectFactory objectFactory = new ObjectFactory();
    private final MeteringService meteringService;
    private final MeteringGroupsService meteringGroupsService;
    private final ch.iec.tc57._2011.meterreadings_.ObjectFactory payloadObjectFactory = new ch.iec.tc57._2011.meterreadings_.ObjectFactory();
    private final MeterReadingsGenerator meterReadingsGenerator = new MeterReadingsGenerator();
    private final Clock clock;

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
            MeterReadingsPayloadType meterReadingsPayloadType = objectFactory.createMeterReadingsPayloadType();
            payload.value = meterReadingsPayloadType;
            meterReadingsPayloadType.setMeterReadings(payloadObjectFactory.createMeterReadings());
            Interval interval = getInterval(request);

            addForRequestedUsagePoints(request, meterReadingsPayloadType, interval);
            addForRequestedEndDevices(request, meterReadingsPayloadType, interval);
            addForRequestedUsagePointGroups(request, meterReadingsPayloadType, interval);
            addForRequestedEndDeviceGroups(request, meterReadingsPayloadType, interval);
        }
    }

    GetMeterReadingsPortImpl(MeteringService meteringService, MeteringGroupsService meteringGroupsService, Clock clock) {
        this.meteringService = meteringService;
        this.meteringGroupsService = meteringGroupsService;
        this.clock = clock;
    }

    private void addForEndDevice(MeterReadingsPayloadType meterReadingsPayloadType, com.elster.jupiter.metering.EndDevice endDevice, Interval interval) {
        if (endDevice instanceof Meter) {
            addForMeter(meterReadingsPayloadType, (Meter) endDevice, interval);
        }
    }

    private void addForEndDeviceGroup(MeterReadingsPayloadType meterReadingsPayloadType, com.elster.jupiter.metering.groups.EndDeviceGroup endDeviceGroup, Interval interval) {
        for (EndDeviceMembership membership : endDeviceGroup.getMembers(interval)) {
            addForMembership(meterReadingsPayloadType, membership);
        }
    }

    private void addForMembership(MeterReadingsPayloadType meterReadingsPayloadType, UsagePointMembership membership) {
        for (Interval subInterval : membership.getIntervals()) {
            addForUsagePoint(meterReadingsPayloadType, membership.getUsagePoint(), subInterval);
        }
    }

    private void addForMembership(MeterReadingsPayloadType meterReadingsPayloadType, EndDeviceMembership membership) {
        for (Interval subInterval : membership.getIntervals()) {
            addForEndDevice(meterReadingsPayloadType, membership.getEndDevice(), subInterval);
        }
    }

    private void addForMeter(MeterReadingsPayloadType meterReadingsPayloadType, Meter meter, Interval interval) {
        Set<Map.Entry<Interval,MeterActivation>> entries = getMeterActivationsPerInterval(meter, interval).entrySet();
        for (Map.Entry<Interval, MeterActivation> entry : entries) {
            meterReadingsGenerator.addMeterReadings(meterReadingsPayloadType.getMeterReadings(), entry.getValue(), entry.getKey());
        }
    }

    private void addForRequestedEndDeviceGroups(GetMeterReadingsRequestType request, MeterReadingsPayloadType meterReadingsPayloadType, Interval interval) {
        for (String endDeviceGroupMrid : requestedEndDeviceGroups(request)) {
            Optional<com.elster.jupiter.metering.groups.EndDeviceGroup> found = meteringGroupsService.findEndDeviceGroup(endDeviceGroupMrid);
            if (found.isPresent()) {
                addForEndDeviceGroup(meterReadingsPayloadType, found.get(), interval);
            }
        }
    }

    private void addForRequestedEndDevices(GetMeterReadingsRequestType request, MeterReadingsPayloadType meterReadingsPayloadType, Interval interval) {
        for (String endDeviceMrid : requestedEndDevices(request)) {
            Optional<com.elster.jupiter.metering.EndDevice> found = meteringService.findEndDevice(endDeviceMrid);
            if (found.isPresent() && found.get() instanceof Meter) {
                addForMeter(meterReadingsPayloadType, (Meter) found.get(), interval);
            }
        }
    }

    private void addForRequestedUsagePointGroups(GetMeterReadingsRequestType request, MeterReadingsPayloadType meterReadingsPayloadType, Interval interval) {
        for (String usagePointGroupMrID : requestedUsagePointGroups(request)) {
            Optional<com.elster.jupiter.metering.groups.UsagePointGroup> found = meteringGroupsService.findUsagePointGroup(usagePointGroupMrID);
            if (found.isPresent()) {
                addForUsagePointGroup(meterReadingsPayloadType, found.get(), interval);
            }
        }
    }

    private void addForRequestedUsagePoints(GetMeterReadingsRequestType request, MeterReadingsPayloadType meterReadingsPayloadType, Interval interval) {
        List<String> usagePointMrids = requestedUsagePoints(request);
        for (String usagePointMrid : usagePointMrids) {
            Optional<com.elster.jupiter.metering.UsagePoint> found = meteringService.findUsagePoint(usagePointMrid);
            if (found.isPresent()) {
                addForUsagePoint(meterReadingsPayloadType, found.get(), interval);
            }
        }
    }

    private void addForUsagePoint(MeterReadingsPayloadType meterReadingsPayloadType, com.elster.jupiter.metering.UsagePoint usagePoint, Interval interval) {
        Set<Map.Entry<Interval,MeterActivation>> entries = getMeterActivationsPerInterval(usagePoint, interval).entrySet();
        for (Map.Entry<Interval, MeterActivation> entry : entries) {
            meterReadingsGenerator.addMeterReadings(meterReadingsPayloadType.getMeterReadings(), entry.getValue(), entry.getKey());
        }
    }

    private void addForUsagePointGroup(MeterReadingsPayloadType meterReadingsPayloadType, com.elster.jupiter.metering.groups.UsagePointGroup usagePointGroup, Interval interval) {
        List<UsagePointMembership> memberships = usagePointGroup.getMembers(interval);
        for (UsagePointMembership membership : memberships) {
            addForMembership(meterReadingsPayloadType, membership);
        }
    }

    private Interval getInterval(GetMeterReadingsRequestType request) {
        Date startTime = request.getStartTime();
        Date endTime = request.getEndTime();
        return new Interval(startTime, endTime);
    }

    private Map<Interval, MeterActivation> getMeterActivationsPerInterval(com.elster.jupiter.metering.UsagePoint usagePoint, Interval interval) {
        Map<Interval, MeterActivation> map = new HashMap<>();
        for (MeterActivation meterActivation : usagePoint.getMeterActivations()) {
            if (meterActivation.getInterval().overlaps(interval)) {
                map.put(intersection(meterActivation, interval), meterActivation);
            }
        }
        return map;
    }

    private Map<Interval, MeterActivation> getMeterActivationsPerInterval(Meter meter, Interval interval) {
        Map<Interval, MeterActivation> map = new HashMap<>();
        for (MeterActivation meterActivation : meter.getMeterActivations()) {
            if (meterActivation.getInterval().overlaps(interval)) {
                map.put(intersection(meterActivation, interval), meterActivation);
            }
        }
        return map;
    }

    private Interval intersection(MeterActivation meterActivation, Interval interval) {
        return meterActivation.getInterval().intersection(interval);
    }

    private List<String> requestedEndDeviceGroups(GetMeterReadingsRequestType request) {
        List<String> endDeviceGroups = new ArrayList<>();
        if (request.getGetMeterReadings() != null) {
            GetMeterReadings getMeterReadings = request.getGetMeterReadings();
            for (EndDeviceGroup endDeviceGroup : getMeterReadings.getEndDeviceGroup()) {
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
