package com.elster.jupiter.metering;

import com.google.common.base.Optional;

import java.util.Date;

public interface ReadingQuality {

    String getComment();

    Date getTimestamp();

    Channel getChannel();

    ReadingQualityType getType();

    long getId();

    void setComment(String comment);

    Optional<BaseReadingRecord> getBaseReadingRecord();

    void save();

    Date getReadingTimestamp();

    void delete();
}
