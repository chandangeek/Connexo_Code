package com.elster.jupiter.metering.cim.impl;

import ch.iec.tc57._2011.meterreadings_.EndDeviceEvent;
import ch.iec.tc57._2011.meterreadings_.Meter;
import ch.iec.tc57._2011.meterreadings_.MeterReading;
import ch.iec.tc57._2011.meterreadings_.MeterReadings;
import ch.iec.tc57._2011.meterreadings_.ObjectFactory;
import ch.iec.tc57._2011.meterreadings_.Reading;
import ch.iec.tc57._2011.meterreadings_.ReadingQuality;
import ch.iec.tc57._2011.meterreadings_.UsagePoint;
import com.elster.jupiter.cbo.Status;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.metering.readings.ProfileStatus;
import com.elster.jupiter.util.collections.BinarySearch;
import com.elster.jupiter.util.time.Interval;
import com.google.common.collect.FluentIterable;

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
            addBaseReadings(meterReading, getReadings(channel, interval), getValidationQualities(channel, interval));
        }
    }

    private List<com.elster.jupiter.metering.ReadingQuality> getValidationQualities(Channel channel, Interval interval) {
        List<com.elster.jupiter.metering.ReadingQuality> qualities = channel.findReadingQuality(interval);
        Collections.sort(qualities, new Comparator<com.elster.jupiter.metering.ReadingQuality>() {
            @Override
            public int compare(com.elster.jupiter.metering.ReadingQuality first, com.elster.jupiter.metering.ReadingQuality second) {
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

    public void addEndDeviceEvents(MeterReadings meterReadings, EndDevice endDevice, Interval interval) {
        MeterReading meterReading = payloadObjectFactory.createMeterReading();
        meterReadings.getMeterReading().add(meterReading);
        meterReading.setMeter(createMeter(endDevice));
        addEndDeviceEvents(meterReading, endDevice, interval);
    }
    private void addEndDeviceEvents(MeterReading meterReading, EndDevice endDevice, Interval interval) {
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

    void addBaseReadings(MeterReading meterReading, List<? extends BaseReadingRecord> intervalReadings, List<com.elster.jupiter.metering.ReadingQuality> validationQualities) {
        BinarySearch<Date, com.elster.jupiter.metering.ReadingQuality> binarySearch = binarySearchByTimestamp(validationQualities);
        for (BaseReadingRecord baseReading : intervalReadings) {
            List<com.elster.jupiter.metering.ReadingQuality> relevantQualities = relevantQualities(baseReading, binarySearch);
            addReadingsPerReadingType(meterReading, baseReading, relevantQualities);
        }
    }

    private BinarySearch<Date, com.elster.jupiter.metering.ReadingQuality> binarySearchByTimestamp(List<com.elster.jupiter.metering.ReadingQuality> validationQualities) {
        return BinarySearch.in(validationQualities).using(new BinarySearch.Key<Date, com.elster.jupiter.metering.ReadingQuality>() {
                @Override
                public Date getKey(com.elster.jupiter.metering.ReadingQuality readingQuality) {
                    return readingQuality.getReadingTimestamp();
                }
            });
    }

    private List<com.elster.jupiter.metering.ReadingQuality> relevantQualities(BaseReadingRecord baseReading, BinarySearch<Date, com.elster.jupiter.metering.ReadingQuality> binarySearch) {
        int first = binarySearch.firstOccurrence(baseReading.getTimeStamp());
        if (first < 0) {
            return Collections.emptyList();
        }
        int last = binarySearch.lastOccurrence(baseReading.getTimeStamp());
        return binarySearch.getList().subList(first, last + 1);
    }

    private void addReadingsPerReadingType(MeterReading meterReading, BaseReadingRecord baseReading, List<com.elster.jupiter.metering.ReadingQuality> relevantQualities) {
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

    void createReading(MeterReading meterReading, BaseReading baseReading, Reading.ReadingType readingType, List<com.elster.jupiter.metering.ReadingQuality> relevantQualities) {
        if (baseReading.getValue() == null) {
            return;
        }
        Reading reading = payloadObjectFactory.createReading();
        reading.setReadingType(readingType);
        reading.setReportedDateTime(baseReading.getTimeStamp());
        reading.setValue(baseReading.getValue().toString());
        if (baseReading instanceof IntervalReading) {
            ProfileStatus profileStatus = ((IntervalReading) baseReading).getProfileStatus();
            for (ProfileStatus.Flag flag : profileStatus.getFlags()) {
                if (flag.getCimCode().isPresent()) {
                    ReadingQuality readingQuality = payloadObjectFactory.createReadingQuality();
                    ReadingQuality.ReadingQualityType qualityType = payloadObjectFactory.createReadingQualityReadingQualityType();
                    qualityType.setRef(flag.getCimCode().get());
                    readingQuality.setReadingQualityType(qualityType);
                    readingQuality.setTimeStamp(baseReading.getTimeStamp());
                }
            }
        }
        for (com.elster.jupiter.metering.ReadingQuality quality : relevantQualities) {
            ReadingQuality readingQuality = payloadObjectFactory.createReadingQuality();
            ReadingQuality.ReadingQualityType readingQualityType = payloadObjectFactory.createReadingQualityReadingQualityType();
            readingQualityType.setRef(quality.getType().getCode());
            readingQuality.setReadingQualityType(readingQualityType);
            readingQuality.setTimeStamp(quality.getTimestamp());
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