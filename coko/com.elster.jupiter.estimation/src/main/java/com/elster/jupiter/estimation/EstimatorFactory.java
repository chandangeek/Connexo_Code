package com.elster.jupiter.estimation;

import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;

import java.util.List;
import java.util.Map;

public interface EstimatorFactory {

    List<String> available();

    Estimator create(String implementation, Map<String, Object> props);

    Estimator createTemplate(String implementation);

    void estimate(MeterActivation meterActivation);

    void estimate(MeterActivation meterActivation, ReadingType readingType);

}
