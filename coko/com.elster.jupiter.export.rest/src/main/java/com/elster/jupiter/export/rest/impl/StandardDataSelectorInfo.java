/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.export.ValidatedDataOption;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.time.rest.RelativePeriodInfo;

import java.util.ArrayList;
import java.util.List;

public class StandardDataSelectorInfo {

    public long id;
    public IdWithNameInfo deviceGroup;
    public IdWithNameInfo usagePointGroup;
    public RelativePeriodInfo exportPeriod;
    public boolean exportContinuousData;
    public boolean exportUpdate; // only used from FE to BE
    public boolean exportComplete;
    public boolean exportAdjacentData;
    public RelativePeriodInfo updatePeriod;
    public RelativePeriodInfo updateWindow;
    public ValidatedDataOption validatedDataOption;
    public List<ReadingTypeInfo> readingTypes = new ArrayList<>();
    public List<EventTypeInfo> eventTypeCodes = new ArrayList<>();

}