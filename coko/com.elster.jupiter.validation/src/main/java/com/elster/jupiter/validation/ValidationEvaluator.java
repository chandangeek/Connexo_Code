package com.elster.jupiter.validation;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.util.time.Interval;
import com.google.common.base.Optional;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by tgr on 5/09/2014.
 */
public interface ValidationEvaluator {

    public ValidationResult getValidationResult(Collection<? extends ReadingQuality> qualities);

    boolean isAllDataValidated(MeterActivation meterActivation);

    List<DataValidationStatus> getValidationStatus(Channel channel, List<? extends BaseReading> readings);

    List<DataValidationStatus> getValidationStatus(Channel channel, Interval interval);

    List<DataValidationStatus> getValidationStatus(Channel channel, List<? extends BaseReading> readings, Interval interval);

    boolean isValidationEnabled(Meter meter);

    boolean isValidationEnabled(Channel channel);

    Optional<Date> getLastChecked(Meter meter, ReadingType readingType);
}
