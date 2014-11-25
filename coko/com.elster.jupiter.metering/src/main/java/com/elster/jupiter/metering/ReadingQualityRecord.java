package com.elster.jupiter.metering;

import java.time.Instant;
import java.util.Optional;

import com.elster.jupiter.metering.readings.ReadingQuality;

public interface ReadingQualityRecord extends ReadingQuality {

    Instant getTimestamp();

    Channel getChannel();

    void setComment(String comment);

    Optional<BaseReadingRecord> getBaseReadingRecord();

    void save();

    Instant getReadingTimestamp();

    void delete();

    long getVersion();

	boolean isActual();

    boolean hasEditCategory();

    boolean hasValidationCategory();

    boolean isSuspect();

    boolean isMissing();
    
    boolean isError();

    void makePast();

    void makeActual();

	boolean hasReasonabilityCategory();
}
