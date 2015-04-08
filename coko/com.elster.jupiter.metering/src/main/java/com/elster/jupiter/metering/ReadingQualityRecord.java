package com.elster.jupiter.metering;

import java.time.Instant;
import java.util.Optional;

public interface ReadingQualityRecord extends com.elster.jupiter.metering.readings.ReadingQuality {

    Instant getTimestamp();

    Channel getChannel();

    CimChannel getCimChannel();

    ReadingType getReadingType();

    long getId();

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
