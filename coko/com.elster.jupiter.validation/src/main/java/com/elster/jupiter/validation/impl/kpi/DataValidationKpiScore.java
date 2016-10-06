/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl.kpi;

import java.time.Instant;

interface DataValidationKpiScore extends Comparable<DataValidationKpiScore> {

    long getTotalSuspects();

    long getChannelSuspects();

    long getRegisterSuspects();

    long getAllDataValidated();

    Instant getTimestamp();

    long getThresholdValidator();

    long getMissingValuesValidator();

    long getReadingQualitiesValidator();

    long getRegisterIncreaseValidator();

}