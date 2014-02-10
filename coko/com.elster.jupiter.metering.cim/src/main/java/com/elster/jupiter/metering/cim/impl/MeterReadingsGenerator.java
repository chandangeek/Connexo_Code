package com.elster.jupiter.metering.cim.impl;

import ch.iec.tc57._2011.meterreadings_.EndDeviceEvent;
import ch.iec.tc57._2011.meterreadings_.Meter;
import ch.iec.tc57._2011.meterreadings_.MeterReading;
import ch.iec.tc57._2011.meterreadings_.MeterReadings;
import ch.iec.tc57._2011.meterreadings_.ObjectFactory;
import ch.iec.tc57._2011.meterreadings_.Reading;
import ch.iec.tc57._2011.meterreadings_.UsagePoint;
import com.elster.jupiter.cbo.Status;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.util.time.Interval;
import com.google.common.collect.FluentIterable;

import java.util.List;

public class MeterReadingsGenerator {

    private final ObjectFactory payloadObjectFactory = new ObjectFactory();
    private final ReadingTypeFilter filter;

    public MeterReadingsGenerator() {
        filter = AllReadingTypes.INSTANCE;
    }

    public MeterReadingsGenerator(ReadingTypeFilter filter) {
        this.filter = filter;
    }

    public MeterReadings createMeterReadings(MeterActivation meterActivation, Interval interval) {
        MeterReadings meterReadings = payloadObjectFactory.createMeterReadings();
        addMeterReadings(meterReadings, meterActivation, interval);
        return meterReadings;
    }

    public MeterReadings createMeterReadings(EndDevice endDevice, Interval interval) {
        MeterReadings meterReadings = payloadObjectFactory.createMeterReadings();
        MeterReading meterReading = createMeterReading(meterReadings, createMeter(endDevice), null);
        addEndDeviceEvents(meterReading, endDevice, interval);
        return meterReadings;
    }

    public void addMeterReadings(MeterReadings meterReadings, MeterActivation meterActivation, Interval interval) {
        MeterReading meterReading = createMeterReading(meterReadings, createMeter(meterActivation.getMeter().orNull()), createUsagePoint(meterActivation.getUsagePoint().orNull()));
        if (meterActivation.getMeter().isPresent()) {
            addEndDeviceEvents(meterReading, meterActivation.getMeter().get(), interval);
        }
        for (Channel channel : meterActivation.getChannels()) {
            addBaseReadings(meterReading, getReadings(channel, interval));
        }
    }

    private UsagePoint createUsagePoint(com.elster.jupiter.metering.UsagePoint usagePoint) {
        if (usagePoint == null) {
            return null;
        }
        UsagePoint point = payloadObjectFactory.createUsagePoint();
        point.setMRID(usagePoint.getMRID());
        return point;
    }

    public void addEndDeviceEvents(MeterReading meterReading, EndDevice endDevice, Interval interval) {
        List<EndDeviceEventRecord> deviceEvents = endDevice.getDeviceEvents(interval);
        for (EndDeviceEventRecord deviceEvent : deviceEvents) {
            EndDeviceEvent endDeviceEvent = payloadObjectFactory.createEndDeviceEvent();
            endDeviceEvent.setEndDeviceEventType(toEndDeviceEventType(deviceEvent.getEventType()));
            endDeviceEvent.setMRID(deviceEvent.getMRID());
            endDeviceEvent.setCreatedDateTime(deviceEvent.getCreatedDateTime());
            endDeviceEvent.setIssuerID(deviceEvent.getIssuerID());
            endDeviceEvent.setIssuerTrackingID(deviceEvent.getIssuerTrackingID());
            endDeviceEvent.setSeverity(deviceEvent.getSeverity());
            endDeviceEvent.setStatus(toStatus(deviceEvent.getStatus()));
            meterReading.getEndDeviceEvents().add(endDeviceEvent);
        }
    }

    private ch.iec.tc57._2011.meterreadings_.Status toStatus(Status status) {
        if (status == null) {
            return null;
        }
        ch.iec.tc57._2011.meterreadings_.Status state = payloadObjectFactory.createStatus();
        state.setDateTime(status.getDateTime());
        state.setReason(status.getReason());
        state.setRemark(status.getRemark());
        state.setValue(status.getValue());
        return state;
    }

    private EndDeviceEvent.EndDeviceEventType toEndDeviceEventType(com.elster.jupiter.metering.events.EndDeviceEventType eventType) {
        EndDeviceEvent.EndDeviceEventType type = payloadObjectFactory.createEndDeviceEventEndDeviceEventType();
        type.setRef(eventType.getMRID());
        return type;
    }

    private List<? extends BaseReadingRecord> getReadings(Channel channel, Interval interval) {
        if (channel.isRegular()) {
            return channel.getIntervalReadings(interval);
        }
        return channel.getRegisterReadings(interval);
    }

    void addBaseReadings(MeterReading meterReading, List<? extends BaseReadingRecord> intervalReadings) {
        for (BaseReadingRecord baseReading : intervalReadings) {
            addReadingsPerReadingType(meterReading, baseReading);
        }
    }

    private void addReadingsPerReadingType(MeterReading meterReading, BaseReadingRecord baseReading) {
        for (ReadingType readingType : FluentIterable.from(baseReading.getReadingTypes()).filter(filter)) {
            createReading(meterReading, baseReading, createReadingType(readingType));
        }
    }

    MeterReading createMeterReading(MeterReadings meterReadings, Meter meter, UsagePoint usagePoint) {
        MeterReading meterReading = payloadObjectFactory.createMeterReading();
        meterReading.setMeter(meter);
        meterReading.setUsagePoint(usagePoint);
        meterReadings.getMeterReading().add(meterReading);
        return meterReading;
    }

    Meter createMeter(EndDevice meter) {
        if (meter == null) {
            return null;
        }
        Meter value = payloadObjectFactory.createMeter();
        value.setMRID(meter.getMRID());
        return value;
    }

    Reading createReading(MeterReading meterReading, BaseReading baseReading, Reading.ReadingType readingType) {
        Reading reading = payloadObjectFactory.createReading();
        reading.setReadingType(readingType);
        reading.setReportedDateTime(baseReading.getTimeStamp());
        reading.setValue(baseReading.getValue().toString());
        meterReading.getReadings().add(reading);
        return reading;
    }

    Reading.ReadingType createReadingType(ReadingType type) {
        Reading.ReadingType readingType = payloadObjectFactory.createReadingReadingType();
        readingType.setRef(type.getMRID());
        return readingType;
    }
}