package com.elster.jupiter.estimation;

import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;

import java.util.List;
import java.util.Optional;

public interface EstimationService {

    String COMPONENTNAME = "EST";

    List<Estimator> getAvailableEstimators();

    Optional<Estimator> getEstimator(String implementation);

    EstimationResult estimate(MeterActivation meterActivation);

    EstimationResult estimate(MeterActivation meterActivation, ReadingType readingType);

    EstimationResult estimate(MeterActivation meterActivation, ReadingType readingType, Estimator estimator);
}
