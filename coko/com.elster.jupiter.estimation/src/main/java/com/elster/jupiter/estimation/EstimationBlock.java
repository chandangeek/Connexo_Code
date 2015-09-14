package com.elster.jupiter.estimation;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;

import java.util.List;

public interface EstimationBlock {

    Channel getChannel();

    CimChannel getCimChannel();

    ReadingType getReadingType();

    List<? extends Estimatable> estimatables();

    void setReadingQualityType(ReadingQualityType readingQualityType);

    ReadingQualityType getReadingQualityType();
}
