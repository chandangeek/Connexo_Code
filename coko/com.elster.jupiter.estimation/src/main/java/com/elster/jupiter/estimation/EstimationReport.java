/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation;

import com.elster.jupiter.metering.ReadingType;

import java.util.Map;

public interface EstimationReport {
    Map<ReadingType, EstimationResult> getResults();
}
