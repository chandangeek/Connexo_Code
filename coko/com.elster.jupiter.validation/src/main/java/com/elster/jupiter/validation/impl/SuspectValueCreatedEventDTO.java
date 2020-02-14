package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.ReadingQualityRecord;

/**
 * The class SuspectValueCreatedEventDTO we'll be using to push adittional
 * data about the Suspect Reading into event queue
 *
 * @author edragutan
 */
public class SuspectValueCreatedEventDTO {

    private long channelId;
    private String readingType;
    private long readingTimeStamp;

    public long getChannelId() {
        return channelId;
    }

    public void setChannelId(long channelId) {
        this.channelId = channelId;
    }

    public String getReadingType() {
        return readingType;
    }

    public void setReadingType(String readingType) {
        this.readingType = readingType;
    }

    public long getReadingTimeStamp() {
        return readingTimeStamp;
    }

    public void setReadingTimeStamp(long readingTimeStamp) {
        this.readingTimeStamp = readingTimeStamp;
    }

    /**
     * @return data transfer object containing information about suspect reading
     */
    public static SuspectValueCreatedEventDTO fromMeterAndReadingQualityRecord(final ReadingQualityRecord readingQualityRecord) {
        final SuspectValueCreatedEventDTO suspectValueCreatedEventDTO = new SuspectValueCreatedEventDTO();
        suspectValueCreatedEventDTO.setChannelId(readingQualityRecord.getChannel().getId());
        suspectValueCreatedEventDTO.setReadingType(readingQualityRecord.getReadingType().getMRID());
        suspectValueCreatedEventDTO.setReadingTimeStamp(readingQualityRecord.getReadingTimestamp().toEpochMilli());
        return suspectValueCreatedEventDTO;
    }
}
