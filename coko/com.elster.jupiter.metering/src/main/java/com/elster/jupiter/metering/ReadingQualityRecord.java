package com.elster.jupiter.metering;

import com.google.common.base.Optional;

import java.util.Date;

public interface ReadingQualityRecord extends com.elster.jupiter.metering.readings.ReadingQuality {

    Date getTimestamp();

    Channel getChannel();

    ReadingQualityType getType();

    long getId();

    void setComment(String comment);

    Optional<BaseReadingRecord> getBaseReadingRecord();

    void save();

    Date getReadingTimestamp();

    void delete();

    long getVersion();

	boolean isActual();

    boolean hasEditCategory();

    boolean hasValidationCategory();

    boolean isSuspect();

    boolean isMissing();

    void makePast();

    void makeActual();
}
