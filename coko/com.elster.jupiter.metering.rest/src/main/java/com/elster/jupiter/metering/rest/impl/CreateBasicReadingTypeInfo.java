/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.rest.impl;

import java.util.List;

public class CreateBasicReadingTypeInfo {
    public String mRID;
    public String aliasName;
    public Integer basicMacroPeriod;
    public Integer basicAggregate;
    public Integer basicMeasuringPeriod;
    public Integer basicAccumulation;
    public Integer basicFlowDirection;
    public Integer basicCommodity;
    public Integer basicMeasurementKind;
    public Integer basicUnit;
    public List<Integer> basicTou;
    public List<Integer> basicCpp;
    public List<Integer> basicConsumptionTier;
    public List<Integer> basicPhases;
    public List<Integer> basicMetricMultiplier;

    public CreateBasicReadingTypeInfo() {
    }
}
