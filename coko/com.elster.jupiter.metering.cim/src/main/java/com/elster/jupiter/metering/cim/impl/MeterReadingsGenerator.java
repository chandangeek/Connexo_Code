package com.elster.jupiter.metering.cim.impl;

import ch.iec.tc57._2011.meterreadings.EndDeviceEvent;
import ch.iec.tc57._2011.meterreadings.Meter;
import ch.iec.tc57._2011.meterreadings.MeterReading;
import ch.iec.tc57._2011.meterreadings.MeterReadings;
import ch.iec.tc57._2011.meterreadings.ObjectFactory;
import ch.iec.tc57._2011.meterreadings.Reading;
import ch.iec.tc57._2011.meterreadings.ReadingQuality;
import ch.iec.tc57._2011.meterreadings.UsagePoint;
import com.elster.jupiter.cbo.Status;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.metering.readings.ProfileStatus;
import com.elster.jupiter.util.collections.BinarySearch;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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

    public MeterReadings createMeterReadings(MeterActivation meterActivation, Range<Instant> range) {
        MeterReadings meterReadings = payloadObjectFactory.createMeterReadings();
        addMeterReadings(meterReadings, meterActivation, range);
        return meterReadings;
    }

    public MeterReadings createMeterReadings(EndDevice endDevice, Range<Instant> range) {
        MeterReadings meterReadings = payloadObjectFactory.createMeterReadings();
        MeterReading meterReading = createMeterReading(meterReadings, createMeter(endDevice), null);
        addEndDeviceEvents(meterReading, endDevice, range);
        return meterReadings;
    }

    public void addMeterReadings(MeterReadings meterReadings, MeterActivation meterActivation, Range<Instant> range) {
        MeterReading meterReading = createMeterReading(meterReadings, createMeter(meterActivation.getMeter().orElse(null)), createUsagePoint(meterActivation.getUsagePoint().orElse(null)));
        if (meterActivation.getMeter().isPresent()) {
            addEndDeviceEvents(meterReading, meterActivation.getMeter().get(), range);
        }
        for (Channel channel : meterActivation.getChannels()) {
            addBaseReadings(meterReading, getReadings(channel, range), getValidationQualities(channel, range));
        }
    }

    private List<ReadingQualityRecord> getValidationQualities(Channel channel, Range<Instant> range) {
        List<com.elster.jupiter.metering.ReadingQualityRecord> qualities = channel.findReadingQuality(range);
        Collections.sort(qualities, new Comparator<com.elster.jupiter.metering.ReadingQualityRecord>() {
            @Override
            public int compare(com.elster.jupiter.metering.ReadingQualityRecord first, com.elster.jupiter.metering.ReadingQualityRecord second) {
                return first.getTimestamp().compareTo(second.getTimestamp());
            }
        });
        return qualities;
    }

    private UsagePoint createUsagePoint(com.elster.jupiter.metering.UsagePoint usagePoint) {
        if (usagePoint == null) {
            return null;
        }
        UsagePoint point = payloadObjectFactory.createUsagePoint();
        point.setMRID(usagePoint.getMRID());
        return point;
    }

    public void addEndDeviceEvents(MeterReadings meterReadings, EndDevice endDevice, Range<Instant> range) {
        MeterReading meterReading = payloadObjectFactory.createMeterReading();
        meterReadings.getMeterReading().add(meterReading);
        meterReading.setMeter(createMeter(endDevice));
        addEndDeviceEvents(meterReading, endDevice, range);
    }
    
    private void addEndDeviceEvents(MeterReading meterReading, EndDevice endDevice, Range<Instant> range) {
        List<EndDeviceEventRecord> deviceEvents = endDevice.getDeviceEvents(range);
        for (EndDeviceEventRecord deviceEvent : deviceEvents) {
            EndDeviceEvent endDeviceEvent = payloadObjectFactory.createEndDeviceEvent();
            endDeviceEvent.setEndDeviceEventType(toEndDeviceEventType(deviceEvent.getEventType()));
            endDeviceEvent.setMRID(deviceEvent.getMRID());
            endDeviceEvent.setCreatedDateTime(Date.from(deviceEvent.getCreatedDateTime()));
            endDeviceEvent.setIssuerID(deviceEvent.getIssuerID());
            endDeviceEvent.setIssuerTrackingID(deviceEvent.getIssuerTrackingID());
            endDeviceEvent.setSeverity(deviceEvent.getSeverity());
            endDeviceEvent.setStatus(toStatus(deviceEvent.getStatus()));
            meterReading.getEndDeviceEvents().add(endDeviceEvent);
        }
    }

    private ch.iec.tc57._2011.meterreadings.Status toStatus(Status status) {
        if (status == null) {
            return null;
        }
        ch.iec.tc57._2011.meterreadings.Status state = payloadObjectFactory.createStatus();
        state.setDateTime(Date.from(status.getDateTime()));
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

    private List<? extends BaseReadingRecord> getReadings(Channel channel, Range<Instant> range) {
        if (channel.isRegular()) {
            return channel.getIntervalReadings(range);
        }
        return channel.getRegisterReadings(range);
    }

    void addBaseReadings(MeterReading meterReading, List<? extends BaseReadingRecord> intervalReadings, List<com.elster.jupiter.metering.ReadingQualityRecord> validationQualities) {
        BinarySearch<Instant, com.elster.jupiter.metering.ReadingQualityRecord> binarySearch = binarySearchByTimestamp(validationQualities);
        for (BaseReadingRecord baseReading : intervalReadings) {
            List<com.elster.jupiter.metering.ReadingQualityRecord> relevantQualities = relevantQualities(baseReading, binarySearch);
            addReadingsPerReadingType(meterReading, baseReading, relevantQualities);
        }
    }

    private BinarySearch<Instant, com.elster.jupiter.metering.ReadingQualityRecord> binarySearchByTimestamp(List<com.elster.jupiter.metering.ReadingQualityRecord> validationQualities) {
        return BinarySearch.in(validationQualities).using(ReadingQualityRecord::getReadingTimestamp);
    }

    private List<com.elster.jupiter.metering.ReadingQualityRecord> relevantQualities(BaseReadingRecord baseReading, BinarySearch<Instant, com.elster.jupiter.metering.ReadingQualityRecord> binarySearch) {
        int first = binarySearch.firstOccurrence(baseReading.getTimeStamp());
        if (first < 0) {
            return Collections.emptyList();
        }
        int last = binarySearch.lastOccurrence(baseReading.getTimeStamp());
        return binarySearch.getList().subList(first, last + 1);
    }

    private void addReadingsPerReadingType(MeterReading meterReading, BaseReadingRecord baseReading, List<com.elster.jupiter.metering.ReadingQualityRecord> relevantQualities) {
        for (ReadingType readingType : FluentIterable.from(baseReading.getReadingTypes()).filter(filter)) {
            createReading(meterReading, baseReading, createReadingType(readingType), relevantQualities);
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

    void createReading(MeterReading meterReading, BaseReading baseReading, Reading.ReadingType readingType, List<com.elster.jupiter.metering.ReadingQualityRecord> relevantQualities) {
        if (baseReading.getValue() == null) {
            return;
        }
        Reading reading = payloadObjectFactory.createReading();
        reading.setReadingType(readingType);
        reading.setReportedDateTime(Date.from(baseReading.getTimeStamp()));
        reading.setValue(baseReading.getValue().toString());
        if (baseReading instanceof IntervalReading) {
            ProfileStatus profileStatus = ((IntervalReading) baseReading).getProfileStatus();
            for (ProfileStatus.Flag flag : profileStatus.getFlags()) {
                if (flag.getCimCode().isPresent()) {
                    ReadingQuality readingQuality = payloadObjectFactory.createReadingQuality();
                    ReadingQuality.ReadingQualityType qualityType = payloadObjectFactory.createReadingQualityReadingQualityType();
                    qualityType.setRef(flag.getCimCode().get());
                    readingQuality.setReadingQualityType(qualityType);
                    readingQuality.setTimeStamp(Date.from(baseReading.getTimeStamp()));
                }
            }
        }
        for (com.elster.jupiter.metering.ReadingQualityRecord quality : relevantQualities) {
            ReadingQuality readingQuality = payloadObjectFactory.createReadingQuality();
            ReadingQuality.ReadingQualityType readingQualityType = payloadObjectFactory.createReadingQualityReadingQualityType();
            readingQualityType.setRef(quality.getType().getCode());
            readingQuality.setReadingQualityType(readingQualityType);
            readingQuality.setTimeStamp(Date.from(quality.getTimestamp()));
            reading.getReadingQualities().add(readingQuality);
        }
        meterReading.getReadings().add(reading);
    }

    Reading.ReadingType createReadingType(ReadingType type) {
        Reading.ReadingType readingType = payloadObjectFactory.createReadingReadingType();
        readingType.setRef(type.getMRID());
        return readingType;
    }
}