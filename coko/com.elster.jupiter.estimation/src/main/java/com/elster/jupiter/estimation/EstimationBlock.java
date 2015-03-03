package com.elster.jupiter.estimation;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingType;

import java.util.List;

public interface EstimationBlock {

    Channel getChannel();

    ReadingType getReadingType();

    List<? extends Estimatable> estimatables();

    void applyEstimations();
}
