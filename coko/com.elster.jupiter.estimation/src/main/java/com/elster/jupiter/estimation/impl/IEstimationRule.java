/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.properties.PropertySpec;

import java.util.Map;

interface IEstimationRule extends EstimationRule {

    void delete();

    void save();

    void toggleActivation();

    void clearReadingTypes();

    PropertySpec getPropertySpec(String name);

    void rename(String name);

    void setPosition(int position);

    void setProperties(Map<String, Object> map);

    Estimator createNewEstimator();

    boolean appliesTo(Channel channel);
}
