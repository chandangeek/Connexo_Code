package com.elster.jupiter.metering.cim.soap.impl;

import ch.iec.tc57._2011.getmeterreadings.FaultMessage;
import ch.iec.tc57._2011.getmeterreadings.GetMeterReadingsPort;
import ch.iec.tc57._2011.getmeterreadings_.EndDevice;
import ch.iec.tc57._2011.getmeterreadings_.GetMeterReadings;
import ch.iec.tc57._2011.getmeterreadings_.UsagePoint;
import ch.iec.tc57._2011.getmeterreadings_.UsagePointGroup;
import ch.iec.tc57._2011.getmeterreadingsmessage.GetMeterReadingsRequestType;
import ch.iec.tc57._2011.getmeterreadingsmessage.MeterReadingsPayloadType;
import ch.iec.tc57._2011.getmeterreadingsmessage.ObjectFactory;
import ch.iec.tc57._2011.meterreadings_.MeterReading;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.schema.message.ReplyType;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.cim.impl.MeterReadingsGenerator;
import com.elster.jupiter.util.time.Interval;
import com.google.common.base.Optional;

import javax.jws.WebParam;
import javax.xml.ws.Holder;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

class GetMeterReadingsPortImpl implements GetMeterReadingsPort {


    private final ObjectFactory objectFactory = new ObjectFactory();
    private final MeteringService meteringService;
    private final ch.iec.tc57._2011.meterreadings_.ObjectFactory payloadObjectFactory = new ch.iec.tc57._2011.meterreadings_.ObjectFactory();
    private final MeterReadingsGenerator meterReadingsGenerator = new MeterReadingsGenerator();

    GetMeterReadingsPortImpl(MeteringService meteringService) {
        this.meteringService = meteringService;
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
        List<String> endDeviceMrids = new ArrayList<>();
        List<String> usagePointMrids = new ArrayList<>();
        List<String> usagePointGroups = new ArrayList<>();

        if (request != null) {
            Interval interval = getInterval(request);
            if (request.getGetMeterReadings() != null) {
                GetMeterReadings getMeterReadings = request.getGetMeterReadings();
                for (UsagePoint usagePoint : getMeterReadings.getUsagePoint()) {
                    usagePointMrids.add(usagePoint.getMRID());
                }
                for (EndDevice endDevice : getMeterReadings.getEndDevice()) {
                    endDeviceMrids.add(endDevice.getMRID());
                }
                for (UsagePointGroup usagePointGroup : getMeterReadings.getUsagePointGroup()) {
                    usagePointGroups.add(usagePointGroup.getMRID());
                }
            }
            MeterReadingsPayloadType meterReadingsPayloadType = objectFactory.createMeterReadingsPayloadType();
            payload.value = meterReadingsPayloadType;
            meterReadingsPayloadType.setMeterReadings(payloadObjectFactory.createMeterReadings());

            for (String usagePointMrid : usagePointMrids) {
                Optional<com.elster.jupiter.metering.UsagePoint> found = meteringService.findUsagePoint(usagePointMrid);
                if (found.isPresent()) {
                    Set<Map.Entry<Interval,MeterActivation>> entries = getMeterActivationsPerInterval(found.get(), interval).entrySet();
                    for (Map.Entry<Interval, MeterActivation> entry : entries) {
                        meterReadingsGenerator.addMeterReadings(meterReadingsPayloadType.getMeterReadings(), entry.getValue(), entry.getKey());
                    }
                }
            }

            for (String endDeviceMrid : endDeviceMrids) {
                Optional<com.elster.jupiter.metering.EndDevice> found = meteringService.findEndDevice(endDeviceMrid);
                if (found.isPresent() && found.get() instanceof Meter) {
                    Set<Map.Entry<Interval,MeterActivation>> entries = getMeterActivationsPerInterval((Meter) found.get(), interval).entrySet();
                    for (Map.Entry<Interval, MeterActivation> entry : entries) {
                        meterReadingsGenerator.addMeterReadings(meterReadingsPayloadType.getMeterReadings(), entry.getValue(), entry.getKey());
                    }
                }
            }

            for (String usagePointGroup : usagePointGroups) {

            }
        }
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


    private Interval getInterval(GetMeterReadingsRequestType request) {
        Date startTime = request.getStartTime();
        Date endTime = request.getEndTime();
        return new Interval(startTime, endTime);
    }

    private MeterReading createMeterReading() {
        return payloadObjectFactory.createMeterReading();
    }

    private Principal determinePrincipal() {
        return null;
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
}
