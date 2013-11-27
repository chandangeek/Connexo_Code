package com.elster.jupiter.metering;

import java.util.Date;

public interface ReadingQuality {

    String getComment();

    Date getTimestamp();

    Channel getChannel();

    ReadingQualityType getType();

    long getId();

    void setComment(String comment);

    BaseReadingRecord getBaseReadingRecord();

    void save();
}
