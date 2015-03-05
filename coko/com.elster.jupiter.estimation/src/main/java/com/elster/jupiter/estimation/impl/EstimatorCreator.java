package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.estimation.Estimator;

import java.util.Map;

interface EstimatorCreator {
    public Estimator getTemplateEstimator(String implementation);

    public Estimator getEstimator(String implementation, Map<String, Object> props);
}
