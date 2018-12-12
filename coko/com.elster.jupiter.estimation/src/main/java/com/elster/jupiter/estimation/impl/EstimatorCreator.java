/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.estimation.Estimator;

import java.util.Map;

interface EstimatorCreator {

    Estimator getTemplateEstimator(String implementation);

    Estimator getEstimator(String implementation, Map<String, Object> props);
}
